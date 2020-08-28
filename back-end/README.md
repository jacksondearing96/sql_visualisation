Keeping this experimental stuff in this folder just until we get something more concrete working which can actually be used and integrated into our actual project directory.

Things to know:

- This is a maven project, download maven if you need to 

- It is structured for IntelliJ so install that and build/run the application from inside IntelliJ

- The pom.xml file should express the dependency on the actual .jar file which is the facebook presto-parser. The first time you build the project maven should take care of downloading that and putting it in the right place so that it works.

- Only important file that I wrote is Main.java file (src/main/java/Main.java)

- Just for experimenting all the propic sql files have been concatenated into resources/AllStatements.sql which is what is read and parsed.

- The presto-parser uses the terminology 'selects' which is what we have been calling 'columns' just fyi

- My java is most likely horrid :/ 
