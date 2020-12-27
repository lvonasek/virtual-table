# Virtual Table

This is a very simple system for playing cards between friends written in JavaSE.

![image2](https://github.com/lvonasek/virtual-table/blob/main/screenshot.jpg?raw=true)

## Instalation

To make the system working you need own SQL DB server and compile the system in Eclipse:

1) Open DB.java and enter SQL login data there

2) Set in DB.java also custom password for game admin

3) Run the system as "admin" with password you set

4) Run reset_db.sql in the admin window

5) Edit sql/reset_players.sql and set custom user names and passwords (possibly changing user's private area rectangle)

6) Run reset_players.sql and reset_game_romme.sql in the admin window

Now you can export the project as JAR (executable package) and provide it with other folders to your friends

Notes:
* JRE (Java Runtime Environment) has to be installed
* on older laptops you might want to run it using following parameters: java -Xms512M -Xmx1024M -jar virtual-table.jar 2

## Adding more games

To add a game you need to scan all game cards in JPG format and put it into data folder. Then you need to create SQL script to set default DB table (see reset_game_romme.sql for details). There is needed to put relative path for every card from front and rear side. Additionally x and y position in range 0-1 and order (using %RND% will set random order on every run). Position should be by default inside "admin" area (defined in reset_players.sql).

To add a dice add following item into the SQL script:

`('data/common/1.png', 'data/common/1.png', '0.865', '0.85', '0')`
