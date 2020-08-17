create or replace view "%(db)s"."prediction_obj_v2" as  
with  
	prediction_properties as ( 
	select address_detail_pid 
		, score 
		, case_a 
		, case_b 
		, case_c 
		, case_d 
		, user_sf_id 
		, 'market' as pred_type 
	from "%(db)s"."market_insight" 
 
	union all 
 
	select distinct address_detail_pid 
		, crm_market_score as score 
		, case_a 
		, case_b 
		, case_c 
		, case_d 
		, null as user_sf_id 
		, 'crm' as pred_type 
	from "%(db)s"."customer_insight" 
	), 
	np_predictions as ( 
	select i1.address_id as address_detail_pid  
		 , i1.suburb  
		 , i1.postcode  
		 , i1.state_abbreviation as state  
		 , i1.address_streetaddress  
		 , concat(i3.address_streetaddress, ' ', i3.suburb) as record_sale_addr 
		 , i1.rea_portalurl  
		 , i1.dom_portalurl  
 
		 , i1.n_latitude  
		 , i1.n_longitude  
 
		 , i1.bedrooms  
		 , i1.bathrooms  
		 , i1.carspaces  
		 , i1.saleprice  
 
		 , i1.rentdate_final  
		 , i1.saledate_final  
		 , i1.withdrawn_date  
		 , i1.status as listing_status  
		  
		 , i1.agent  
		 , i1.agent_email  
		 , i1.agent_phone  
		 , i1.brand  
		 , i1.property_type_regroup  
 
		 , i1.owner_occ  
		 , i1.years_held  
		 , i1.near_listings_yesterday  
		 , i1.all_near_listings  
		 , i1.daysonmarket  
		 , i1.ubuntu  
		 , i1.days_since_withdrawn  
		 , i1.days_since_withdrawn_bucket  
 
		 , i2.score 
		 , i2.case_a 
		 , i2.case_b 
		 , i2.case_c 
		 , i2.case_d 
		 , i2.user_sf_id 
		 , i2.pred_type 
	from "datalake_dbicd"."master_market_data" as i1 
	inner join prediction_properties as i2 
	on i1.address_id = i2.address_detail_pid 
 
	left join "datalake_dbicd"."record_sale_in_street_yesterday" i3  
	on i1.street = i3.street and i1.suburb = i3.suburb and i1.postcode = i3.postcode and i1.state_abbreviation = i3.state_abbreviation  
	) 
select * 
from np_predictions###  