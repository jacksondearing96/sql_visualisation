create or replace view "%(db)s"."market_score" as 
with  
	market as (  
	select i1.address_id as address_detail_pid  
		, i1.suburb 
		, i1.postcode  
		, i1.years_held  
		, i1.state_abbreviation as state 
 
		, case when owner_occ = 'Yes' 
				and years_held > i2.median_years_held 
				and (near_listings_yesterday IS NOT NULL or all_near_listings IS NOT NULL) then 1  
			else 0  
		end as case_a  
 
		, case when ubuntu = 'pytorch'  
				and owner_occ = 'Yes'  
				and days_since_withdrawn <= 30 then 1  
			else 0  
		end as case_b  
 
		, case when ubuntu = 'keras'  
				and owner_occ = 'Yes' then 1  
			else 0  
		end as case_c  
 
		, case when i3.record_sold_price_flag = 1  
				and owner_occ = 'Yes'  
				and years_held > i2.median_years_held then 1  
			else 0  
		end as case_d  
 
		, 10 as wt_case_a  
 
		, case when ubuntu = 'pytorch'  
				and days_since_withdrawn < 10 then 30  
			when ubuntu = 'pytorch'  
				and days_since_withdrawn between 10 and 30 then 20  
			else 0  
		end as wt_case_b  
 
		, 30 as wt_case_c  
		, 5 as wt_case_d  
 
	from "datalake_dbicd"."master_market_data" i1  
	left join "datalake_dbicd"."median_length_of_ownership" i2  
	using (state_abbreviation, postcode, suburb)  
	 
	left join "datalake_dbicd"."record_sale_in_street_yesterday" i3  
	on i1.street = i3.street and i1.suburb = i3.suburb and i1.postcode = i3.postcode and i1.state_abbreviation = i3.state_abbreviation  
 
	where (i1.status <> 'current' or i1.status is null) 
	and i1.ubuntu <> 'keras' 
	and i1.postcode in ( 
		select distinct reveal_postcode  
		from "%(db)s"."subscriber_postcode"  
		where reveal_postcode is not null 
		)  
	and i1.address_id not in ( 
		select distinct address_detail_pid 
		from "%(db)s"."customer_insight" 
		) 
	),  
	market_scoring as (  
	select *  
		, case_a * wt_case_a  
		+ case_b * wt_case_b  
		+ case_c * wt_case_c  
		+ case_d * wt_case_d  
		as score  
	from market  
	) 
select *  
	, rank() over (partition by state, suburb order by score DESC, address_detail_pid ASC) as score_row_num  
from market_scoring  
where score > 0 and years_held >= 1### 
 
create or replace view "%(db)s"."market_insight" as 
with  
	agent_postcode as ( 
		select id, reveal_postcode  
		from "%(db)s"."agent_detail"  
		cross join unnest(split(distinct prpt__reveal_postcode__c, ';')) as t (reveal_postcode)  
		where prpt__reveal_postcode__c is not null 
	),  
	market_pred_by_agent as ( 
		select a.address_detail_pid, a.postcode, a.score, b.id as user_sf_id, random() as rd 
		from "%(db)s"."market_score" as a 
		left join agent_postcode as b 
		on a.postcode = b.reveal_postcode 
	),  
	assign_market_pred_to_agent as ( 
		select *  
		from ( 
			select * 
				, rank() over (partition by address_detail_pid order by rd asc) as rnk 
			from market_pred_by_agent 
			) 
		where rnk = 1 
	), 
	keep_top_market_pred as ( 
		select * from ( 
			select * 
				, rank() over (partition by user_sf_id order by (score+rd) desc) as user_score_rnk 
			from assign_market_pred_to_agent 
			) 
		where user_score_rnk <= 70 
	), 
	join_market_insight as ( 
		select a.address_detail_pid 
			, a.score 
			, a.case_a 
			, a.case_b 
			, a.case_c 
			, a.case_d 
			, b.user_sf_id 
		from "%(db)s"."market_score" as a  
		inner join keep_top_market_pred as b 
		on a.address_detail_pid = b.address_detail_pid 
	) 
select * 
from join_market_insight###  
 