from flask import Flask, render_template, request

app = Flask(__name__)

@app.route('/')                         # default webpage
def index():
    return render_template('upload.html')


# is called by the html template after file is uploaded
@app.route('/uploader', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        if request.files:
            f = request.files['file']
            f = f.read()                # f can now be treated as a regular file.
            return f                    
# The upload_file is just returning the uploaded file currently
# this will be passed to the sql script in the future
# which will then be redirected to the vue application.


if __name__ == '__main__':
    app.run(debug=True)
