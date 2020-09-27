public class Main {
    public static void main(String[] args) {
        // Use the FileReader for now.
        // In production, this will be redundant - sql will come in string format from the front-end.
        String sql = FileReader.ReadFile("../propic_sql_scripts/test_wildcard.sql");
        DataLineage dataLineage = LineageExtractor.extractLineage(sql);
    }
    
    // Only to test if the  server is still working
    public String outputDataStruct(String name){
        return "[{\n" +
"    \"type\": \"view\",\n" +
"    \"name\": \"%(db)s.note_count_by_agent\",\n" +
"    \"alias\": \"Woosh1\",\n" +
"    \"id\": \"0\",\n" +
"    \"parents_id\": \"1\",\n" +
"    \"children_id\": [\"2\"],\n" +
"    \"columns\": [{\n" +
"        \"name\": \"testa\",\n" +
"        \"alias\": \"test1a\",\n" +
"        \"id\": \"test2a\",\n" +
"        \"sources\": [\"test::APPLE\", \"test::WAMBLE\"]\n" +
"    }, {\n" +
"        \"name\": \"testb\",\n" +
"        \"alias\": \"test1b\",\n" +
"        \"id\": \"test2b\",\n" +
"        \"sources\": [\"test::BANANA\", \"test::WAMBLE2\"]\n" +
"    },\n" +
"     {\n" +
"        \"name\": \"testc\",\n" +
"        \"alias\": \"test1c\",\n" +
"        \"id\": \"test1c\",\n" +
"        \"sources\": [\"test::Oats\", \"test::WAMBLE3\"]\n" +
"    }]\n" +
"},\n" +
"    {\n" +
"    \"type\": \"table\",\n" +
"    \"name\": \"%(db)s.%(crm)s_task\",\n" +
"    \"alias\": \"Woosh1\",\n" +
"    \"id\": \"1\",\n" +
"    \"parents_id\": \"\",\n" +
"    \"children_id\": [\"0\"],\n" +
"    \"columns\": [{\n" +
"        \"name\": \"testa\",\n" +
"        \"alias\": \"test1a\",\n" +
"        \"id\": \"test2a\",\n" +
"        \"sources\": [\"test::APPLE\", \"test::WAMBLE\"]\n" +
"    }, {\n" +
"        \"name\": \"testb\",\n" +
"        \"alias\": \"test1b\",\n" +
"        \"id\": \"test2b\",\n" +
"        \"sources\": [\"test::BANANA\", \"test::WAMBLE2\"]\n" +
"    },\n" +
"        {\n" +
"        \"name\": \"testc\",\n" +
"        \"alias\": \"test1c\",\n" +
"        \"id\": \"test1c\",\n" +
"        \"sources\": [\"test::Oats\", \"test::WAMBLE3\"]\n" +
"    }]\n" +
"},\n" +
"    {\n" +
"    \"type\": \"view\",\n" +
"    \"name\": \"%(db)s.agent_prediction_obj\",\n" +
"    \"alias\": \"Woosh1\",\n" +
"    \"id\": \"2\",\n" +
"    \"parents_id\": [\"3\", \"4\"],\n" +
"    \"children_id\": [\"\"],\n" +
"    \"columns\": [{\n" +
"        \"name\": \"testa\",\n" +
"        \"alias\": \"test1a\",\n" +
"        \"id\": \"test2a\",\n" +
"        \"sources\": [\"test::APPLE\", \"test::WAMBLE\"]\n" +
"    }, {\n" +
"        \"name\": \"testb\",\n" +
"        \"alias\": \"test1b\",\n" +
"        \"id\": \"test2b\",\n" +
"        \"sources\": [\"test::BANANA\", \"test::WAMBLE2\"]\n" +
"    }]\n" +
"},\n" +
"    {\n" +
"    \"type\": \"view\",\n" +
"    \"name\": \"%(db)s.customer_insight\",\n" +
"    \"alias\": \"Woosh1\",\n" +
"    \"id\": \"3\",\n" +
"    \"parents_id\": \"\",\n" +
"    \"children_id\": [\"2\"],\n" +
"    \"columns\": [{\n" +
"        \"name\": \"testa\",\n" +
"        \"alias\": \"test1a\",\n" +
"        \"id\": \"test2a\",\n" +
"        \"sources\": [\"test::APPLE\", \"test::WAMBLE\"]\n" +
"    }, {\n" +
"        \"name\": \"testb\",\n" +
"        \"alias\": \"test1b\",\n" +
"        \"id\": \"test2b\",\n" +
"        \"sources\": [\"test::BANANA\", \"test::WAMBLE2\"]\n" +
"    }]\n" +
"},\n" +
"    {\n" +
"    \"type\": \"table\",\n" +
"    \"name\": \"%(db)s.note_count_by_agent\",\n" +
"    \"alias\": \"Woosh1\",\n" +
"    \"id\": \"4\",\n" +
"    \"parents_id\": \"\",\n" +
"    \"children_id\": [\"2\"],\n" +
"    \"columns\": [{\n" +
"        \"name\": \"testa\",\n" +
"        \"alias\": \"test1a\",\n" +
"        \"id\": \"test2a\",\n" +
"        \"sources\": [\"test::APPLE\", \"test::WAMBLE\"]\n" +
"    }, {\n" +
"        \"name\": \"testb\",\n" +
"        \"alias\": \"test1b\",\n" +
"        \"id\": \"test2b\",\n" +
"        \"sources\": [\"test::BANANA\", \"test::WAMBLE2\"]\n" +
"    }]\n" +
"}\n" +
"]";
    }
    
}