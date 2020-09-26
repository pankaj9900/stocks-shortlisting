# Shortlisting Stocks

## What is this project?
The goal of this project is to shortlist stocks based on the given csv, which contains the holdings of the various Mutual funds. This project aims at taking the csv, and shortlisting top N stocks based on how many MFs hold the particular stock, and their average holdings.
This is **NOT** intended to be a recommendation engine, but is just a tool to help you shortlist a few stocks which you can then research upon and take informed decision.

## Motivation
It is time consuming and tedious to short list stocks to buy, and thus, I wanted a tool that I could just feed the csv files of MF holdings, and it would give me only few shares that are interesting, so that I could then spend my limited time on researching on them.

## Running the project
This is a simple java project built using maven.
The steps to run this are:
1. Checkout the code.
1. Download the csv files that you intend to feed to this system (it mandatorily needs the columns for stock name, and the holdings in %).
1. Modify the property file (more on this below).
1. Run the project as a simple java application.

### Properties and their significance:
1. directoryToScan=csv
    - This is the directory where the csv files are expected to be present (relative path from /resources).
1. columnIndexForStockName=0
    - This is the index at which the stock name column is present in the csv. Indexing starts from 0.
1. columnIndexForStockHoldings=4
    - This is the index at which the stock holdings column is present in the csv. Indexing starts from 0.
1. individualCutoffHoldingsPercent=2.0
    - The cutoff % value for individual stocks in each csv. Any stock holding below this number will be ignored.
1. averageCutOffHoldingsPercent=2.0
    - The cutoff % value for cumulative holding AFTER averaging it across the holdings. The final average holding below this number will be ignored. 
1. topNStocks=20
    - The final count of stocks to be displayed. The sorting is in descending order of Count (# of times the stocks as present in the csv) and and then % Holdings (average, if the count > 1).

##### I used [this](https://trendlyne.com/mutual-fund/mf-all "https://trendlyne.com/mutual-fund/mf-all") URL for downloading the MF portfolio. However, you can use any site as long as the  _Stock Name_  and the  _Holding %_  are present in the csv.

#### Possible Enhancements
1. Calculate weighted average, instead of regular average.
