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