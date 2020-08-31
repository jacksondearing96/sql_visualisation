from flask import Flask, jsonify, render_template, request
from flask_cors import CORS
import jnius_config
jnius_config.set_classpath('.', './java/testeGabriel.jar')
from jnius import autoclass
# Configuration
DEBUG = True

app = Flask(__name__)
app.config.from_object(__name__)

# Enable CORS
CORS(app, resources={r'/*': {'origins': '*'}})


@app.route('/')
def index():
    return render_template('upload.html')


# Agent leads test
AGENT_LEADS = [{
    "type": "view",
    "name": "%(db)s.note_count_by_agent",
    "alias": "Woosh1",
    "id": "0",
    "parents_id": "1",
    "children_id": ["2"],
    "columns": [{
        "name": "testa",
        "alias": "test1a",
        "id": "test2a",
        "sources": ["test::APPLE", "test::WAMBLE"]
    }, {
        "name": "testb",
        "alias": "test1b",
        "id": "test2b",
        "sources": ["test::BANANA", "test::WAMBLE2"]
    }]
},
    {
    "type": "table",
    "name": "%(db)s.%(crm)s_task",
    "alias": "Woosh1",
    "id": "1",
    "parents_id": "",
    "children_id": ["0"],
    "columns": [{
        "name": "testa",
        "alias": "test1a",
        "id": "test2a",
        "sources": ["test::APPLE", "test::WAMBLE"]
    }, {
        "name": "testb",
        "alias": "test1b",
        "id": "test2b",
        "sources": ["test::BANANA", "test::WAMBLE2"]
    }]
},
    {
    "type": "view",
    "name": "%(db)s.agent_prediction_obj",
    "alias": "Woosh1",
    "id": "2",
    "parents_id": ["3", "4"],
    "children_id": [""],
    "columns": [{
        "name": "testa",
        "alias": "test1a",
        "id": "test2a",
        "sources": ["test::APPLE", "test::WAMBLE"]
    }, {
        "name": "testb",
        "alias": "test1b",
        "id": "test2b",
        "sources": ["test::BANANA", "test::WAMBLE2"]
    }]
},
    {
    "type": "view",
    "name": "%(db)s.customer_insight",
    "alias": "Woosh1",
    "id": "3",
    "parents_id": "",
    "children_id": ["2"],
    "columns": [{
        "name": "testa",
        "alias": "test1a",
        "id": "test2a",
        "sources": ["test::APPLE", "test::WAMBLE"]
    }, {
        "name": "testb",
        "alias": "test1b",
        "id": "test2b",
        "sources": ["test::BANANA", "test::WAMBLE2"]
    }]
},
    {
    "type": "table",
    "name": "%(db)s.note_count_by_agent",
    "alias": "Woosh1",
    "id": "4",
    "parents_id": "",
    "children_id": ["2"],
    "columns": [{
        "name": "testa",
        "alias": "test1a",
        "id": "test2a",
        "sources": ["test::APPLE", "test::WAMBLE"]
    }, {
        "name": "testb",
        "alias": "test1b",
        "id": "test2b",
        "sources": ["test::BANANA", "test::WAMBLE2"]
    }]
},
]

"""
Is called by the html template after file is uploaded.
The upload_file is just returning the uploaded file currently
this will be passed to the sql script in the future
which will then be redirected to the vue application.
"""


@app.route('/uploader', methods=['GET', 'POST'])
def upload_file():
    response_object = {'status': 'success'}
    if request.method == "POST" and request.files:
        file = request.files['file']
        response_object['name'] = file.filename
        response_object['tables'] = AGENT_LEADS
    return jsonify(response_object)


"""
Trying to integrate Java with Python Back-end

Add to your Environment Variables:
   PATH C:\Program Files\Java\jdk1.7.0_79\jre\bin\server , C:\Program Files\Java\jdk1.7.0_79\bin
   Follow instructions here: https://pyjnius.readthedocs.io/en/stable/installation.html
"""


@app.route('/javatest', methods=['GET', 'POST'])
def javatest():
    response_object = {'status': 'success'}
    if request.method == 'POST':
        post_data = request.get_json()
        EchoGabriel = autoclass('au.com.piragibe.testegabriel.EchoGabriel')
        sb = EchoGabriel()
        responseText = sb.makeEcho(post_data.get('name'))
        response_object['resp'] = responseText
        return jsonify(response_object)


if __name__ == '__main__':
    app.run(debug=True)
