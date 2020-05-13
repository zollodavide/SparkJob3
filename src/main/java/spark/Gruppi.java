package spark;
import java.util.Map;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import models.AuxStockInfo;
import models.Stock;
import scala.Tuple2;
import scala.Tuple3;
import utility.Parser;
import utility.StockUtility;

public class Gruppi {
	
	private String file1;
	private String file2;
	
	public Gruppi(String file1, String file2) {
		this.file1 = file1;
		this.file2 = file2;
	}
	
	public JavaRDD<Stock> buildStocks() {

		SparkConf conf = new SparkConf().setAppName("Trend");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<Stock> raw1= sc.textFile(file1).map(line -> Parser.parseFile1(line))
				.filter(stock -> stock!=null)
				.filter(stock -> stock.getData().getYear()+1900>=2016 && stock.getData().getYear()+1900<=2018);
		JavaRDD<AuxStockInfo> raw2= sc.textFile(file2).map(line -> Parser.parseFile2(line));
		Map<String, AuxStockInfo> ticker2info= raw2.mapToPair(stock -> new Tuple2<String, AuxStockInfo>(stock.getTicker(), stock))
				.collectAsMap();
		JavaRDD<Stock> stocks = raw1.map(stock -> Parser.completeStockInfo(stock,ticker2info));
		return stocks;
	}
	
	public JavaPairRDD<Tuple3<Integer, Integer, Integer>, String> creaGruppi() {
		
		JavaRDD<Stock> stocks = buildStocks();
		JavaPairRDD<Tuple2<String, Integer>, Integer> aziendaAnno2meanDiff = meanDifference(stocks);
		
		JavaPairRDD<String,Iterable<Tuple2<Integer,Integer>>> azienda2meanDiffByAnno = aziendaAnno2meanDiff
				.mapToPair(tup -> new Tuple2<>(tup._1()._1(), new Tuple2<>(tup._1()._2(), tup._2())))
				.groupByKey();
		
		JavaPairRDD<Tuple3<Integer, Integer, Integer>, String> triple = azienda2meanDiffByAnno
				.mapToPair(tup -> StockUtility.creaTriple(tup))
				.reduceByKey((s1,s2) -> s1 + " || " + s2 + " || ");
		
		stampa(triple);
		
		return triple;
	}

	private void stampa(JavaPairRDD<Tuple3<Integer, Integer, Integer>, String> triple) {
		Map<Tuple3<Integer, Integer, Integer>, String> out = triple.collectAsMap();

		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		
		for(Tuple3<Integer, Integer, Integer> tri : out.keySet()) 
			System.out.println("2016: " + tri._1() 
				+ "%, 2017: " + tri._2()
				+ "%, 2018: " + tri._3()
				+ "% = {" + out.get(tri) + " }");

		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
	}

	private JavaPairRDD<Tuple2<String, Integer>, Integer> meanDifference(JavaRDD<Stock> stocks) {
		JavaPairRDD<Tuple2<String,Integer>,Stock> aziendaAnno2stock = stocks
				.mapToPair(stock -> new Tuple2<>(new Tuple2<>(stock.getAzienda(),stock.getData().getYear()+1900), stock));
	
		JavaPairRDD<Tuple2<String,Integer>,Double> minStocks = aziendaAnno2stock
				.reduceByKey((s1,s2) -> StockUtility.minDateStock(s1, s2))
				.mapToPair(tup -> new Tuple2(tup._1(), tup._2().getClose()));
		
		JavaPairRDD<Tuple2<String,Integer>,Double> maxStocks = aziendaAnno2stock
				.reduceByKey((s1,s2) -> StockUtility.maxDateStock(s1, s2))
				.mapToPair(tup -> new Tuple2(tup._1(), tup._2().getClose()));
		
		JavaPairRDD<Tuple2<String,Integer>,Integer> aziendaAnno2meanDiff = minStocks.join(maxStocks)
				.mapToPair(tup -> new Tuple2(tup._1(), 
						(int)(((tup._2()._2() - tup._2()._1())/tup._2()._1())*100)));
		
		return aziendaAnno2meanDiff;
	}
	
}
