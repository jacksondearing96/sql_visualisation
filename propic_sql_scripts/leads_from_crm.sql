create or replace view "%(db)s"."customer_insight" as 
with  
	sce_union as ( 
	select i5.address_id, i5.acct_sf_id, i5.user_sf_id, i5.spark, null as kafka 
		, 1 as case_f 
		, 0 as case_g 
	from "%(db)s"."np_buyer_vendor_v2" as i5 
 
	union  
 
	select i6.address_id, i6.acct_sf_id, i6.user_sf_id, null as spark, i6.kafka 
		, 0 as case_f 
		, 1 as case_g 
	from "%(db)s"."np_appraisal_v2" as i6 
	), 
	sce_reduced as ( 
	select address_id, acct_sf_id, user_sf_id 
		, min(case_f) as case_f 
		, min(case_g) as case_g 
		, min(spark) as spark 
		, min(kafka) as kafka 
	from sce_union 
	group by address_id, acct_sf_id, user_sf_id 
	), 
	crm as( 
	select i1.address_id as address_detail_pid  
 
		, i4.acct_sf_id 
		, i4.acct_name 
 
		, coalesce(i7.user_sf_id, i4.user_sf_id) as user_sf_id 
		, i7.spark 
		, i7.kafka 
 
		, case when i4.contact_src = 'crm' 
			and i1.years_held > i2.median_years_held 
			and (i1.near_listings_yesterday is not null or i1.all_near_listings is not null) then 1  
		else 0  
		end as case_e  
		, coalesce(i7.case_f, 0) as case_f 
		, coalesce(i7.case_g, 0) as case_g 
 
		, case when i4.contact_src = 'crm' and  
		i1.years_held > i2.median_years_held and  
			(i1.near_listings_yesterday is not null or i1.all_near_listings is not null) then 1  
			else 0  
		end as case_a  
		, case when i1.ubuntu = 'pytorch'  
				and i1.owner_occ = 'Yes'  
				and i1.days_since_withdrawn <= 30 then 1  
			else 0  
		end as case_b  
		, case when i1.ubuntu = 'keras'  
				and i1.owner_occ = 'Yes' then 1  
			else 0  
		end as case_c  
		, case when i3.record_sold_price_flag = 1  
				and owner_occ = 'Yes'  
				and years_held > i2.median_years_held then 1  
			else 0  
		end as case_d  
 
		, 15 as wt_case_e  
		, 40 as wt_case_f  
		, case when i7.kafka between (current_date - INTERVAL '90' DAY) and current_date then 40  
			when i7.kafka < (current_date - INTERVAL '90' DAY) then 20  
		else 0  
		end as wt_case_g  
 
		, 0 as wt_case_a  
		, case when ubuntu = 'pytorch'  
				and days_since_withdrawn < 10 then 30  
			when ubuntu = 'pytorch'  
				and days_since_withdrawn between 10 and 30 then 20  
			else 0  
		end as wt_case_b  
		, 30 as wt_case_c  
		, 5 as wt_case_d  
 
	from "datalake_dbicd"."master_market_data" as i1  
	left join "datalake_dbicd"."median_length_of_ownership" as i2  
	using (state_abbreviation, postcode, suburb)  
 
	left join "datalake_dbicd"."record_sale_in_street_yesterday" i3  
	on i1.street = i3.street and i1.suburb = i3.suburb and i1.postcode = i3.postcode and i1.state_abbreviation = i3.state_abbreviation  
 
	inner join "%(db)s"."np_client_contact_v2" as i4  
	on i1.address_id = i4.address_id  
 
	left join "sce_reduced" as i7 
	on i4.address_id = i7.address_id and i4.acct_sf_id = i7.acct_sf_id 
 
	where (i1.status <> 'current' or i1.status is null) and  
	i1.ubuntu <> 'keras'  
	),  
	crm_scoring as (  
	select *  
		, case_e * wt_case_e 
		+ case_f * wt_case_f 
		+ case_g * wt_case_g 
		as client_agent_score  

		, case_a * wt_case_a 
		+ case_b * wt_case_b 
		+ case_c * wt_case_c 
		+ case_d * wt_case_d 
		as crm_market_score

		, case_e * wt_case_e 
		+ case_f * wt_case_f 
		+ case_g * wt_case_g 
		+ case_a * wt_case_a 
		+ case_b * wt_case_b 
		+ case_c * wt_case_c 
		+ case_d * wt_case_d 
		as score 
	from crm  
	), 
	agent_highest_score as ( 
	select * 
		, rank() over (partition by address_detail_pid, acct_sf_id order by score desc, upper(user_sf_id) asc) as row_number 
	from crm_scoring 
	where score > 0 
	), 
	prop_client_agent_view as ( 
	select * 
	from agent_highest_score 
	where row_number = 1 
	), 
	primary_owner_agent_view as ( 
	select *  
		, case when row_number_1 = 1 then 'Yes' 
			else 'No' 
		end as primary_owner 
	from ( 
		select * 
			, rank() over (partition by address_detail_pid, user_sf_id order by score desc, upper(acct_sf_id) asc) as row_number_1 
		from prop_client_agent_view 
		) 
	), 
	all_owners_agent_view as ( 
	select a.*, b.all_owners 
	from primary_owner_agent_view as a  
	left join ( 
		select address_detail_pid, user_sf_id, array_join(array_agg(acct_name), ', ') as all_owners 
		from primary_owner_agent_view 
		group by address_detail_pid, user_sf_id 
		) as b 
	on a.address_detail_pid = b.address_detail_pid and a.user_sf_id = b.user_sf_id 
	) 
select address_detail_pid 
	, acct_sf_id 
	, acct_name 
	, user_sf_id 
	, spark 
	, kafka 
	, crm_market_score 
	, client_agent_score 
	, case_e 
	, case_f 
	, case_g 
	, case_a 
	, case_b 
	, case_c 
	, case_d 
	, primary_owner 
	, all_owners 
from all_owners_agent_view###  
 