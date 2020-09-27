create or replace view "%(db)s"."note_count_by_agent" as 
select acct_sf_id, user_sf_id, count(*) as cnt  
from ( 
	select b.acct_sf_id, b.user_sf_id 
	from ( 
		select accountid, ownerid, status, cast(activitydate as date) as task_note_date 
		from "%(db)s"."%(crm)s_task" 
	) as a 
	inner join customer_insight as b 
	on a.accountid = b.acct_sf_id and a.ownerid = b.user_sf_id 
	where a.status = 'Completed' and date_diff('day', a.task_note_date, date(now() AT TIME ZONE 'Australia/Sydney')) > 0 
) 
group by acct_sf_id, user_sf_id###  

create or replace view "%(db)s"."agent_prediction_obj" as
select a.address_detail_pid
	, a.acct_sf_id
	, a.acct_name
	, a.user_sf_id
	, a.spark
	, a.kafka
	, a.client_agent_score
	, a.case_e
	, a.case_f
	, a.case_g
	, a.primary_owner
	, a.all_owners
	, b.cnt as note_cnt
from "%(db)s"."customer_insight" as a
left join "%(db)s"."note_count_by_agent" as b
on a.acct_sf_id = b.acct_sf_id and a.user_sf_id = b.user_sf_id###

create or replace view "%(db)s"."client_with_appr_v2" as
with
	a as (
	select id as acct_sf_id
		, name
		, personmobilephone
		, personemail
	from "%(db)s"."%(crm)s_client_contact"
	where personmobilephone is not null or personemail is not null
	),
	bb as (
	select prpt__buyer_acc__c
		, prpt__listing_1__c
		, from_iso8601_timestamp(lastmodifieddate) as bm_record_lmd
	from "%(db)s"."%(crm)s_prpt__buyer_interest_tbl"
	where hadoop = 'Vendor' and prpt__listing_type__c = 'Sale'
	),
	b as (
	select prpt__buyer_acc__c
		, prpt__listing_1__c
	from (
		select *
			, rank() over (partition by prpt__listing_1__c order by bm_record_lmd desc) as rnk
		from bb
		)
	where rnk = 1
	),
	cc as (
	select id as appr_sf_id
		, cast(substr(redhat, 1, 10) as date) as kafka
		, ownerid as appraisal_owner
		, prpt__property__c
	from "%(db)s"."%(crm)s_prpt__appr_tbl"
	where redhat is not null and prpt__status__c = 'Appraised'
	),
	c as (
	select *
	from (
		select *
			, rank() over (partition by prpt__property__c, appraisal_owner order by kafka desc) as rnk
		from cc
		)
	where rnk = 1
	),
	d as (
	select *
	from "%(db)s"."cleaned_prpt_property_view"
	),
	e as (
	select id as user_sf_id
		, firstname as user_firstname
		, lastname as user_lastname
	from "%(db)s"."agent_detail"
	)
select a.*, c.*, d.*, e.*
from a
inner join b
on a.acct_sf_id = b.prpt__buyer_acc__c
inner join c
on b.prpt__listing_1__c = c.appr_sf_id
inner join d
on c.prpt__property__c = d.prop_sf_id
inner join e
on c.appraisal_owner = e.user_sf_id###

create or replace view "%(db)s"."np_appraisal_v2" as
select a.acct_sf_id
	, a.user_sf_id
	, a.kafka
	, b.address_id
	, 'sce_appr' as had_appraisal_flag
from "%(db)s"."client_with_appr_v2" as a
inner join "datalake_dbicd"."cleaned_complete_address_np_view" as b
on a.final_address = b.full_address
where a.kafka > b.saledate_final###

create or replace view "%(db)s"."np_buyer_vendor_v2" as
select a.acct_sf_id
	, a.user_sf_id
	, a.spark
	, b.address_id
	, 'sce_bv' as buyer_vendor_flag
from (
	select final_address, acct_sf_id, user_sf_id, spark
		, rank() over (partition by acct_sf_id, user_sf_id order by spark desc) as rnk
	from "%(db)s"."buyer_vendor_v2"
) as a
inner join "datalake_dbicd"."cleaned_complete_address_np_view" as b
on a.final_address = b.full_address
where a.rnk = 1###

create or replace view "%(db)s"."cleaned_prpt_property_view" as
with
	a as (
	select id as prop_sf_id
		, prpt__rp_lot_plan__c as lot_plan
		, split_part(prpt__rp_lot_plan__c, ' ', 1) as cleaned_lot_plan
		, upper(prpt__rp_address_full__c) as full_address
		, regexp_replace(replace(upper(prpt__rp_address_full__c), ',', ''), '\s{2,}', ' ') as cleaned_full_address
		, upper(prpt__rp_unit_designator__c) as unit_number
		, upper(prpt__rp_street_designator__c) as street_number
		, upper(prpt__rp_street_name__c) as street_name
		, upper(prpt__rp_street_extensions__c) as street_type
		, upper(prpt__rp_locality_name__c) as suburb
		, lpad(prpt__rp_postcode__c, 4, '0') as postcode
		, upper(prpt__rp_state_code__c) as state
	from "%(db)s"."%(crm)s_prpt__property__c"
	where prpt__rp_address_full__c is not null
	),
	b as (
	select *
		, regexp_extract(cleaned_full_address, '^([0-9]+/[0-9]+)(-[0-9]+)([a-zA-Z0-9 ]+$)', 1) ||
		regexp_extract(cleaned_full_address, '^([0-9]+/[0-9]+)(-[0-9]+)([a-zA-Z0-9 ]+$)', 3) as tmp_addr
	from a
	)
select *
	, coalesce(tmp_addr, cleaned_full_address) as final_address
from b###

create or replace view "%(db)s"."prpt_prop_with_client_contact_v2" as
with
	a as (
	select *
	from "%(db)s"."cleaned_prpt_property_view"
	),
	b as (
	select prpt__Property__c
		, prpt__Client__c
	from "%(db)s"."%(crm)s_prpt__property_client_link_tbl"
	where prpt__Status__c = 'k8s'
	),
	c as (
	select id as acct_sf_id
		, prpt__external_system_id__c as acct_ext_id
		, PERSONMOBILEPHONE, PERSONEMAIL, NAME, LASTNAME, FIRSTNAME, MIDDLENAME
		, ownerid as user_sf_id
	from "%(db)s"."%(crm)s_client_contact"
	where personmobilephone is not null or personemail is not null
	)
select a.*, c.*
from a
inner join b
on a.prop_sf_id = b.prpt__Property__c
inner join c
on b.prpt__Client__c = c.acct_sf_id###


create or replace view "%(db)s"."np_client_contact_v2" as
with
	a as (
	select address_id
		, full_address
	from "datalake_dbicd"."cleaned_complete_address_np_view"
	),
	b as (
	select acct_sf_id
		, name as acct_name
		, user_sf_id
		, final_address
	from "%(db)s"."prpt_prop_with_client_contact_v2"
	),
	c as (
	select a.*, b.*
	from a
	inner join b
	on a.full_address = b.final_address
	)
select distinct address_id
	, 'crm' as contact_src
	, acct_sf_id
	, acct_name
	, user_sf_id
from c###

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


create or replace view "%(db)s"."buyer_vendor_v2" as
with
	a as (
	select *
	from "%(db)s"."cleaned_prpt_property_view"
	),
	b as (
	select prpt__Property__c
		, prpt__Client__c
	from "%(db)s"."%(crm)s_prpt__property_client_link_tbl"
	where prpt__Status__c = 'k8s'
	),
	c as (
	select id as acct_sf_id
		, name
		, personmobilephone
		, personemail
	from "%(db)s"."%(crm)s_client_contact"
	where personmobilephone is not null or personemail is not null
	),
	d as (
	select prpt__buyer_acc__c
		, hadoop
		, prpt__listing_1__c
		, cast(substr(createddate, 1, 10) as date) as spark
	from "%(db)s"."%(crm)s_prpt__buyer_interest_tbl"
	where hadoop in ('snowflake', 'terraform')
	),
	e as (
	select id as listing_sf_id
		, ownerid as listing_owner
	from "%(db)s"."%(crm)s_prpt__appr_tbl"
	where prpt__sale_or_lease__c = 'Sale'
	),
	f as (
	select id as user_sf_id
		, firstname as user_firstname
		, lastname as user_lastname
	from "%(db)s"."agent_detail"
	)
select a.*, c.*, d.*, e.*, f.*
from a
inner join b
on a.prop_sf_id = b.prpt__Property__c
inner join c
on b.prpt__Client__c = c.acct_sf_id
inner join d
on c.acct_sf_id = d.prpt__buyer_acc__c
inner join e
on d.prpt__listing_1__c = e.listing_sf_id
inner join f
on e.listing_owner = f.user_sf_id
where spark between (current_date - INTERVAL '180' DAY) and current_date and
	a.final_address not in (
			select full_address
			from "datalake_dbicd"."cleaned_complete_address_np_view"
			where listingsource = 'BUY' and status = 'current')###
