# Sound City Project - Sound Meter
## Project goal
The main goal of this project is to allow low-cost, large-scale, sufficiently-accurate acoustic monitoring campaign which offer users a series of straightforward educational opportunities for becoming more aware in the acoustic domain and learning about noise metering and acoustic principles measurements directly on their devices in an easy manner.

## Motivation
1. Transportation, different industries, and construction etc. are expanding more and more
2. Unawareness about noise pollution
3. Statisticians or citizens want to learn about noise need efficient, easy and user-friendly system for analysis

## Architecture overview
Application records noise levels in dB using phone microphone and sends the data to RabbitMQ queue. Odysseus accesses the data from RabbitMQ queue and performs necessary operations. Odysseus sends the data back to RabbitMQ queue and the application captures the data from rabbitMQ queue. The processed data is then stored in FireStore, which is then used for querying to get the results in the app.


## Educational opportunities for awareness
Offering users a series of straightforward educational opportunities for becoming more aware in the acoustic domain.

## View history of highest collected noise values
Offering users a set of correct measurements that have been recorded throughout the week


