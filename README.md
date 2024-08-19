## What you get

The application in this repository: 
* creates an H2 database in the root directory of the project;
* sets up the database structure according to the entities;
* seeds some initial data and runs the financing algorithm.

## What you need to do

You need to implement the financing algorithm according to the specification. The algorithm has to
calculate the results of the financing and persist them. The invoices financed in a one application run are considered 
to be "financed" and must not be financed in the subsequent applications runs. 

To store the results of the financing, you will have to adjust the data structure. You are free to create 
new entities and adjust the existing ones.

Your entry point is `FinancingService` class. Naturally, you may create additional
classes, if needed. You can also add new relations, new entities or fields to existing ones. You may also 
use any third-party dependencies you need. 

If you don't like something in the provided code, you are free to change it. You're also free to adjust the
seeding data if you wish to make it more representative.

## Financing algorithm specification

The terminology used here is described in more detail in the Glossary section.

The financing algorithm should be applied separately to each invoice in the database which was not financed yet.

For each non-financed `Invoice`:
1. select the single financing `Purchaser`;
2. calculate financing results: applied financing term, rate and date; early payment amount; 
3. save financing results with link to selected `Purchaser` for the `Invoice` in the database: 
 all monetary values should be stored in `cents` units, rates in `bps` units.

A `Purchaser` is eligible for financing of the `Invoice`, if:
1. the `Purchaser` has set up the settings for the invoice's `Creditor` (has a `PurchaserFinancingSettings` 
  defined for this `Creditor`);
2. the financing term of the invoice (duration between the current date and the maturity date of the invoice) 
  is greater or equal to the value `Purchaser.minimumFinancingTermInDays` for this `Purchaser`;
3. the `Purchaser`'s financing rate for the invoice doesn't exceed the `Creditor.maxFinancingRateInBps` value 
  for the invoice's `Creditor`. 

Of all purchasers eligible for financing, select the one with the lowest financing rate. This will be the 
`Purchaser` that finances the invoice.

Take performance considerations into account: the total amount of invoices in the database could be 
tens of millions of records, with tens of thousands to be financed.

### Example

The rates are measured in bps (basis points). One basis point is 0,01%, or 0,0001.

Suppose today is 2023-05-27. We have 2 purchasers Purchaser1 and Purchaser2 and one creditor Creditor1.
Purchaser1 has set up the annual rate of 50 bps for the Creditor1. Purchaser2 has set up 40 bps for the same creditor.
The Creditor1 has set up 4 bps as maximum financing rate.

The Creditor1 has a single invoice with the value of 10 000,00 EUR and the maturity date of 2023-06-26. 
The financing term of the invoice (duration between today and maturity date) is then 30 days. 

When we run the financing, the financing rate for Purchaser1 for this invoice should be calculated as 
50 bps * 30 days / 360 days/year = 4 bps, the financing rate for Purchaser2 would be 
40 bps * 30 days / 360 days/year = 3 bps. The Purchaser1's financing rate is greater than the maximum financing
rate set up by the Creditor, so only Purchaser2 wins the financing of the invoice.

The early payment amount for the invoice is 10 000,00 EUR - 3,00 EUR = 9 997,00 EUR.

## What we'd like to see

* implementation of the financing algorithm; 
* persisting of the financing results;
* appropriate logging of financing process for observability;
* tests verifying that your solution is correct;
* any documentation you think is necessary for your solution;
* results of performance testing. On typical quad-core x86-64 workstation, we expect to process 10,000 unpaid invoices 
 in under 30 seconds, even when the database holds over 1,000,000 previously financed invoices;
* in case any functional or non-functional requirement are not achieved due to lack of time, 
 provide textual explanation of further steps to explore and implement.

## Glossary

* **Creditor** - a company that has sold some goods to the **Debtor**
* **Debtor** - a company that has purchased some goods from the **Creditor**
* **Invoice** - according to this document, the **Debtor** is to pay to the **Creditor** for the purchased goods 
  on a specific date in the future called **maturity date**.  
* **Maturity date** - the date when the **Creditor** expects the payment from the **Debtor**. If the **Invoice**
is financed, and the **Creditor** already got their money early from the **Purchaser**, this is the date when
  the **Debtor** pays to the **Purchaser** instead.
* **Purchaser** - a bank that is willing to finance the **Invoice** (i.e. provide money for this invoice early 
  to the **Creditor**), with some **interest** subtracted. The **Purchaser** receives the full amount of money 
  from the **Debtor** on **maturity date**, thus receiving their **interest**.  
* **Financing date** - the date on which the financing has occurred.  
* **Financing Term** - the duration in days between the **financing date** and the **Invoice**'s
  **maturity date**. The financing is essentially a loan given by the **Purchaser** to the **Creditor** for the 
  duration of the term, with a certain **financing rate** and responsibility of the **Debtor** to pay back the loan.
* **bps** - basis points, a unit of measure for the rates. 1 bps = 0.01% = 0.0001.
* **Annual Rate** - the interest (in bps) that the **Purchaser** expects to get for the term of 360 days.
* **Financing Rate** - the actual financing rate for a particular **Invoice**, proportional to its financing term. 
  Calculated as `financingRate = annualRate * financingTerm / 360`
* **Early payment amount** - the amount of money paid by the **Purchaser** to the **Creditor** for the particular
financed invoice on **financing date**. This amount is less than the value of the invoice.

# Solution


### Performance
This was the challenging part. In the FinancingServicePerformanceIT test class there is a test that persists over 10000
unpaid invoices and then processes them. With my initial solution (using no batching for processing) I was getting around
40 seconds on a 10 core workstation. I tried to improve it using indexes on the columns used in the queries like: 
creditor_id, purchaser_id, minimumFinancingTermInDays, maturityDate, etc, but I didn't notice some big changes.
So I decided to quit on the part with indexing and I converted the JPQL query into native one, and I was surprised that
it didn't help a lot as well. However, I didn't investigate why due to lack of time.
Then I used the batching solution using Pageable, and that gave me over 50% of performance. On my machine I got an average
of 17 seconds for 10005 unpaid invoices. By the way I used batching as well to persist them initially and I managed to do that
in less than 4 seconds. However, not sure how the performance will behave on a quad-core workstation.
And my final step to improve the performance was to use parallel stream processing and I managed to process the same
amount of invoices in less than 8s. But the downside of the parallel processing is that time to time I was getting
transaction management issues due to thread safety. It definitely deserves more attention, but it requires
more time to careful work with transaction concurrency.

Im curious as well what performance we'll get if instead of purchaserFinancingSettingsRepository.findEligiblePurchaserSetting()
specified query, we'll have purchaserFinancingSettingsRepository.findByCreditor() spring data method, and further
filtering and calculations we'll be done using java api. Theoretically, fetching a large number of rows from the database
and processing them in memory would negatively impact performance.

Some more ideas:
1. Query Optimization
   Projections: Instead of fetching entire entities, fetch only the required columns using projections.
2. Use Caching
   Second-Level Cache: Enable Hibernate’s second-level cache for frequently accessed entities like Creditor, Purchaser,
   and PurchaserFinancingSettings. This can drastically reduce database load for repeated reads.
   Or even use some third party caching solutions like Redis, Hazelcast, etc.
3. Database-Specific Optimizations
   Analyze Execution Plan: Use database’s explain plan feature to analyze the query execution plan and look for potential bottlenecks.
4. Database Partitioning
   If the dataset is large, we should consider partitioning the database tables based on logical criteria (e.g., creditor or purchaser).

### Final notes
I used Spring Data Jpa whenever possible to get quick results for basic operations.

In order to easily visualize the entity relations and data, I created a docker-compose.yml file that creates a postgres
instance.
To use it:
1. Run  docker compose up -d
2. From the application.properties file, just comment the properties related to h2 db and uncomment the ones related to postgres.

The IT tests are using a separate h2 db. 



