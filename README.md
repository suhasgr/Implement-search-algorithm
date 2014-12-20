Implement-search-algorithm
==========================

TF-IDF search

Based on the Lucene index we can design and implement efficient retrieval algorithms.

F(q,doc)  =  sigma( C(qi,doc)/length(doc) * log(1 + (N/k(qi)))

q is the user query, doc is the target, qi is the query term , c(qi,doc) is count of term qi in document doc
N is total number of documnets, k(qi) is total number of doc having term qi. 

For each given query,  code will do 
1. Parse the query using Standard Analyzer 
2. Calculate the relevance score for each query term, and 
3. Calculate the relevance score 𝐹 𝑞, 𝑑𝑜𝑐 .
