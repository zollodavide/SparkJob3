package utility;
import models.Stock;
import scala.Tuple2;
import scala.Tuple3;

public class StockUtility {
	
	public static Stock minDateStock(Stock s1, Stock s2) {
		if(s1.getData().compareTo(s2.getData()) < 0)
			return s1;
		else return s2;
		
	}
	
	public static Stock maxDateStock(Stock s1, Stock s2) {
		if(s1.getData().compareTo(s2.getData()) > 0)
			return s1;
		else return s2;
		
	}

	public static Tuple2<Tuple3<Integer, Integer, Integer>, String> creaTriple(Tuple2<String,
			Iterable<Tuple2<Integer, Integer>>> t) {
		Iterable<Tuple2<Integer, Integer>> tuples= t._2();
		Integer M2016 = 0;
		Integer M2017 = 0;
		Integer M2018= 0;
		for(Tuple2<Integer,Integer> tup : tuples ) {
			if (tup._1() == 2016)
				M2016 = tup._2();
			else if (tup._1() == 2017)
				M2017 = tup._2();
			else
				M2018 = tup._2();
		}
		return new Tuple2<Tuple3<Integer, Integer, Integer>, String>(  
				new Tuple3<Integer, Integer, Integer>(M2016,M2017,M2018), t._1());

	}

}
