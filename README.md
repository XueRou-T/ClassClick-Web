### SERVERS TO ACTIVATE

**1. MySQL Server**
// Open this terminal and keep it
cd C:\myWebProject\mysql\bin
mysqld --console

// Open another terminal
cd C:\myWebProject\mysql\bin
mysql -u myuser -p


// After entering password, load these paths [ONLY ONCE]
SOURCE C:/myWebProject/IM2073-Web/database/schema.sql;
SOURCE C:/myWebProject/IM2073-Web/database/seed.sql;

// Verification and testing
SOURCE C:/myWebProject/IM2073-Web/database/queries.sql;

 → ctrl + c to shut down server

# Access server
http://localhost:9999

# Deploy project
- Copy src/main/webapp → C:\myWebProject\tomcat\webapps\eshop.
- Move all the html files dorectly under eshop

### Development
# Compile servlets
cd \myWebProject\tomcat\webapps\eshop\WEB-INF\classes
javac -cp ..\..\..\..\lib\servlet-api.jar;. *.java


# Start Tomcat
cd C:\myWebProject\tomcat\bin
startup.bat

 → ctrl + c to shut down serve

# Run app
http://localhost:9999/eshop/login.html

