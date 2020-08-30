from flask import Flask, render_template, request

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('upload.html')

"""
Is called by the html template after file is uploaded.
The upload_file is just returning the uploaded file currently
this will be passed to the sql script in the future
which will then be redirected to the vue application.
"""
@app.route('/uploader', methods=['GET', 'POST'])
def upload_file():
    if request.method == "POST" and request.files:
        return request.files["file"].read()

if __name__ == '__main__':
    app.run(debug=True)
