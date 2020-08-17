create table if not exists "datalake_dbicd"."sce_weights" as 
select * from ( 
	values  
	(1, 'wt_case_a', array[10]), 
	(2, 'wt_case_b', array[30, 20]), 
	(3, 'wt_case_c', array[30]), 
	(4, 'wt_case_d', array[5]), 
	(5, 'wt_case_e', array[15]), 
	(6, 'wt_case_f', array[40]), 
	(7, 'wt_case_niu_h', array[5]), 
	(8, 'wt_case_niu_i', array[5]),  
	(9, 'wt_case_g', array[40, 20]), 
	(10, 'wt_case_niu_j', array[5]) 
) as t(id, sce, wt)### 
 
create or replace view "datalake_dbicd"."median_length_of_ownership" as (  
	select state_abbreviation, postcode, suburb  
		 , approx_percentile(years_held, 0.5) as median_years_held  
	from "datalake_dbicd"."master_market_data"  
	group by state_abbreviation, postcode, suburb  
	order by state_abbreviation, postcode, suburb  
)###  
 
create or replace view "%(db)s"."subscriber_postcode" as  
select distinct reveal_postcode  
from "%(db)s"."agent_detail"  
cross join unnest(split(distinct prpt__reveal_postcode__c, ';')) as t (reveal_postcode)  
where prpt__reveal_postcode__c is not null### 
 
 