create or replace view "datalake_dbicd"."record_sale_in_street_yesterday" as   
with   
	a as (  
		select address_id  
			, address_streetaddress  
			, street  
			, suburb  
			, postcode  
			, state_abbreviation  
			, status  
			, first_listed  
			, saledate_final  
			, price_guide  
			, 1 as record_sold_price_flag  
		from "datalake_dbicd"."master_market_data"  
		where date_diff('day', saledate_final, date(now() AT TIME ZONE 'Australia/Sydney')) = 1   
	),  
	b as (  
		select street, suburb, postcode, state_abbreviation  
		 , case when min_sold_price_deltas < min_sold_price_base then min_sold_price_base  
			else min_sold_price_deltas  
		   end as min_sold_price  
		from (  
			select min(price_guide) as min_sold_price_deltas  
			 , min(saleprice) as min_sold_price_base  
			 , street  
			 , suburb  
			 , postcode  
			 , state_abbreviation  
			from "datalake_dbicd"."master_market_data"  
			group by street, suburb, postcode, state_abbreviation  
			)  
	)	  
select a.*, b.min_sold_price  
from a  
inner join b  
using (street, suburb, postcode, state_abbreviation)  
where a.status = 'sold' and a.price_guide >= b.min_sold_price###  
 