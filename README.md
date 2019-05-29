## TELEGRAM WEBHOOK BOT

#### Expose the web server on Internet
With serveo you can expose a port on your local machine mapped to a a given domain
````
ssh -R freydema.serveo.net:80:localhost:3000 serveo.net
````
A request sent to https://freydema.serveo.net/ will be forwarded to localhost:3000

#### Set the webhook on telegram

````
POST 
https://api.telegram.org/bot<TOKEN>/setWebhook
{"url":"https://freydema.serveo.net/hello"}
````

#### Data received on the webhook

