import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;




public class easySearch 
{	
	
	public static void main(String [] args) throws IOException, ParseException
	{
		String indexPath = args[0];
		String queryString = args[1];
		
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		
		long totalNumberOfDoc = indexReader.maxDoc();
		
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT",analyzer); // checking in only TEXT field of the documents
		Query query = parser.parse(queryString); 
		
		Set<Term> queryTerms = new LinkedHashSet<Term>(); 
		query.extractTerms(queryTerms);
		
		DefaultSimilarity defaultSimilarity = new DefaultSimilarity();
		List<AtomicReaderContext> leafContexts = indexReader.getContext().reader().leaves();
		
		HashMap<Integer,HashMap<Integer,Integer>> docQueryMap = new HashMap<Integer,HashMap<Integer,Integer>>();
		
		
		for(int leaf = 0; leaf < leafContexts.size() ; ++leaf )
		{
			AtomicReaderContext leafContext = leafContexts.get(leaf);
			
			int startDocNumber = leafContext.docBase;
			int queryNumber = 0;
			for(Term t: queryTerms)
			{
				++queryNumber;
				int doc = 0;
				DocsEnum docEnum = MultiFields.getTermDocsEnum(leafContext.reader(), MultiFields.getLiveDocs(leafContext.reader()), "TEXT", new BytesRef(t.text()));
				while((doc = docEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS)
				{
					if(!docQueryMap.containsKey(docEnum.docID()+startDocNumber))
					{
						HashMap<Integer,Integer> queryCount = new HashMap<Integer,Integer>();
						docQueryMap.put(docEnum.docID()+startDocNumber, queryCount);
					}
					HashMap<Integer,Integer> queryCount = docQueryMap.get(docEnum.docID()+startDocNumber);
					queryCount.put(queryNumber, docEnum.freq());	
				}
			}
		}
		
		
		for(int leaf = 0; leaf < leafContexts.size() ; ++leaf )
		{
			AtomicReaderContext leafContext = leafContexts.get(leaf);
			
			int startDocNumber = leafContext.docBase;
			int numberOfDoc = leafContext.reader().maxDoc();
			for(int startDoc = startDocNumber; startDoc < (startDocNumber + numberOfDoc) ; ++startDoc)
			{ 
				int queryNumber = 0;
				float rank = 0;
				float normDocLength = defaultSimilarity.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(startDoc-startDocNumber));
				if(docQueryMap.containsKey(startDoc))
				{
					++queryNumber;
					for(Term t: queryTerms)
					{
						long numberOfdocsHaveQueryt = indexReader.docFreq(new Term("TEXT",t.text()));
						HashMap<Integer,Integer> queryCount = docQueryMap.get(startDoc);
						if(queryCount.containsKey(queryNumber))
						{
							float weight = (float) ((queryCount.get(queryNumber)/normDocLength)*Math.log10((1+(totalNumberOfDoc/numberOfdocsHaveQueryt))));
							System.out.println("Relevant Score for query term  "+t.text()+"   in Document   "+(1+startDoc)+"    = "+weight);
							rank+=weight;
						}
					}
				}
				System.out.println("Relevant Score for document = "+(1+startDoc)+"    = "+rank);
			}
		}
		
		
	}

}
