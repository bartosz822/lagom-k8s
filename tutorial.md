# MISOWS Lagom GKE
MISOWS Lagom GKE

## Wstęp

Celem tutorialu jest pokazanie w jaki sposób można uruchomić prostą aplikację napisaną z użyciem mikroserwisowego frameworka **lagom** https://www.lagomframework.com na chmurowej platformie kubernetes w tym przypadku **Google Kubernetes Engine**. Instrukcja krok po kroku ukazuje w jaki sposób utworzyć klaster, skonfigurować projekt i dokonać wdrożenia tak, aby aplikacja działała poprawnie na chmurowej platformie w sposób rozproszony.  


## Wymagania

Na komputerze z którego wykonywane będą polecenia należy mieć zainstalowane następujące narzędzia: 

- sbt https://www.scala-sbt.org/release/docs/Setup.html
- gcp-sdk https://cloud.google.com/sdk/install
- docker https://docs.docker.com/install/
- kubectl https://kubernetes.io/docs/tasks/tools/install-kubectl/


## Zakładanie darmowego konta na GCP
1. Jeśli nie masz załóż konto google - https://accounts.google.com/signup/v2
2. Przejdź na stronę: https://cloud.google.com/free/ i zaaplikuj o darmową wersję próbną GCP
![](https://paper-attachments.dropbox.com/s_4F8BDF79F4BE4B9AB684660D0524F5FCEBF2378C0401A89573E9E9B00B13E87D_1578319265408_image.png)

3. Na stronie należy podać swoje dane osobowe takie jak email, adres oraz w celu potwierdzenia tożsamości numer karty kredytowej. Podczas 12 miesięcznego okresu próbnego dostajemy 300$, które możemy wykorzystać w dowolny sposób. Po jego zakończeniu karta nie zostanie obciążona żadnymi opłatami dopóki nie przejdziemy na wersję "niedarmową” GCP.
4. Po założeniu konta powinniśmy zobaczyć taki widok:
![](https://paper-attachments.dropbox.com/s_4F8BDF79F4BE4B9AB684660D0524F5FCEBF2378C0401A89573E9E9B00B13E87D_1578319565145_image.png)



## Stworzenie klastra kubernetes na GCP
1. Wybierz z menu `Kubernetes Engine` → `Klastry`
2. Poczekaj aż GCP zainicjuje usługę Kubernetes Engine
3. Kilknij utwórz klaster i wybierz szablon klaster standardowy
4. W formularzu wybierz nastepujące opcje
    1. W polu **nazwa** wpisz nazwę jaką chcesz przydzielić klastrowi (lagom-gke)
    2. W polu **strefa** wybierz jeden z regionów `europe`
    3. W polu **Pule węzłów** wybierz węzły `n1-standard-2` 
    4. Kliknij więcej opcji i włącz **autoskalowanie węzłów** podając od 2 do 4 węzłów
    5. Kliknij utwórz klaster
![](https://paper-attachments.dropbox.com/s_4F8BDF79F4BE4B9AB684660D0524F5FCEBF2378C0401A89573E9E9B00B13E87D_1578321448160_image.png)

![](https://paper-attachments.dropbox.com/s_4F8BDF79F4BE4B9AB684660D0524F5FCEBF2378C0401A89573E9E9B00B13E87D_1578321563244_image.png)

## Inicjalizacja gcp sdk

Aby móc zarzadzać klastrem z poziomu terminala należy mieć skonfigurowane narzędzie gcp-sdk zgodnie z instrukcją: https://cloud.google.com/sdk/docs/quickstarts. Najważniejsza dla nas jest możliwość przsyłania obrazów dockerowych do chmurowego rejestru kontenerów oraz możliwość zarządzania stworzonym przez nas klastrem.
 

## Konfiguracja dockera

Po inicjalizacji gcp-sdk należy skonfigurować dockera. W tym celu wpisz w terminalu:
 `gcloud auth configure-docker`
 

## Konfiguracja kubectl

Aby skonfigurować narzędzie kubectl do zarządzania stworzonym przez nas klastrem w terminalu należy wpisać:  `gcloud container clusters get-credentials lagom-gke`, gdzie `lagom-gke` zamienić można na swoją nazwę klastra. 
Po wykonaniu polecenia, aby zweryfikować, że wszystko działa poprawnie należy użyć polecenia: 
`kubectl config current-context`,  które powinno zwrócić skofigurowany wcześniej klaster.


## Stworzenie projektu hello-world lagom

Kod źródłowy aplikacji można przygotować samemu podąrzając za kolejnymi krokami instrukcji, bądź ściągnąć z repozytorium: https://github.com/bartosz822/lagom-k8s.


1. W terminalu wpisz: `sbt new lagom/lagom-scala.g8` i wybierz nazwę projektu `hello` a następnie domyślną konfigurację.
2. Otwórz projekt w preferowanym ide
3. Przejdź w terminalu do folderu z projektem 
4. W terminalu uruchom konsolę `sbt` i skompiluj projekt komendą `compile`
5. Uruchom aplikację w środowisku testowym poleceniem `runAll`
6. Wpisz w przeglądarce adres http://localhost:9000/api/hello/World - strona powinna zwrócić zdanie `Hello, World!`

Więcej informacji o aplikacji znajduje się na stronie https://www.lagomframework.com/documentation/1.6.x/scala/UnderstandHelloScala.html. Aplikacja hello world posiada kilka różnych funkcjonalności, z którymi zapoznamy się podczas dalszych części tutorialu. Składa się ona z 2 mikroserwisów,  bazy danych Cassandra oraz brokera wiadomości Kafka. Wszyskie komponenty należy uruchomić później na klastrze Kubernetes. 
W tym momencie przejrzyj kod aplikacji oraz sprawdź jej funkcjonalności.


## Konfiguracja aplikacji

Aby aplikacja działała poprawnie w środowisku rozproszonym na klastrze k8s należy ją odpowiednio skonfigurować.

**Konfiguracja sbt**
W pliku **build.sbt** projektu w ustawieniach aplikacji należy podać adres repozytorium do którego zuploadowane mają być obrazy aplikacji. W pliku dodaj zmienną
`val dockerRepo = Some("eu.gcr.io/PROJECT_ID")` podmieniając `PROJECT_ID` a nastepnię dodaj `dockerRepository := dockerRepo` do pliku do ustawień dwóch mikroserwisów. 

Do konfiguracji zależności dodaj moduł do znajdywania serwisów w środowisku rozproszonym `lagomScaladslAkkaDiscovery` oraz rozszerzenie do znajdywania serwisów w klastrze Kubernetes (`akka-discovery-kubernetes-api`).  Gotowy plik `build.sbt` powinien wyglądać następująco: https://github.com/bartosz822/lagom-k8s/blob/master/build.sbt.
Po zmianach w pliku `build.sbt` odśwież konsolę sbt  poleceniem `reload` oraz załaduj zmiany w IDE.

**Konfiguracja lagom** 
Konfiguracja aplikacji napisanych z użyciem frameworka lagom znajduje się w plikach `application.conf`. Na potrzeby tego projektu stworzymy dodatkowe pliki `prod-application.conf` które będą zawierać konfigurację używaną podczas działania w środowisku produkcyjnym. Pliki konfiguracyjne wraz z objaśnieniami można znaleźć pod poniższymi linkami:

https://github.com/bartosz822/lagom-k8s/blob/master/hello-impl/src/main/resources/application.conf

https://github.com/bartosz822/lagom-k8s/blob/master/hello-impl/src/main/resources/prod-application.conf

https://github.com/bartosz822/lagom-k8s/blob/master/hello-stream-impl/src/main/resources/application.conf

https://github.com/bartosz822/lagom-k8s/blob/master/hello-stream-impl/src/main/resources/prod-application.conf


Aby aplikacja używała dynamicznego sposobu tworzenia kalstra z wykorzystaniem kubernetes api zmienić należy również implementację metody load w klasie:  `com.helloworld.impl.HelloWorldLoader`

    override def load(context: LagomApplicationContext): LagomApplication =
      new HelloApplication(context) with AkkaDiscoveryComponents

oraz `com.helloworldstream.impl.HelloWorldStreamLoader`

    override def load(context: LagomApplicationContext): LagomApplication =
      new HelloStreamApplication(context) with AkkaDiscoveryComponents


## Stworzenie obrazu aplikacji

Aby uruchomić aplikację w klastrze Kubernetes należy stworzyć i przesłać do Google Container Registry obrazy 2 mikroserwisów.

**Lokalne stworzenie obrazów**

1. W konsoli `sbt` wpisz `docker:publishLocal`
2. Poleceniem `docker images`  sprawdź czy stworzony został obraz aplikacji

**Zuploadowanie kontenerów do Google c****ontainer** **registry**
Wpisz w konsoli sbt `docker:publish` i zaczekaj na wykonanie polecenia.
Na stronie GCP wejdź do widoku narzędzia `[Container Registry](https://console.cloud.google.com/gcr?project=misows-264314)`, powinny tam znajdować się obrazy 2 mikroserwisów.

![](https://paper-attachments.dropbox.com/s_4F8BDF79F4BE4B9AB684660D0524F5FCEBF2378C0401A89573E9E9B00B13E87D_1578327353241_image.png)




## Konfiguracja plików konfiguracyjnych k8s

Stwórz folder **deploy** i utwórz w nim pliki `rbac.yml`, `hello.yml`, `hello-stream.yml`, `zookeper.yml` , `kafka.yml`, `cassandra.yml` oraz `ingress.yml`. Pliki te będą zawierać informacje w jaki sposób skonfigurowac klaster tak, aby działała na nim nasza aplikacja hello world. 

**rbac.yml**
W pliku rbac.yml skonfiguruj rolę pozwalającą naszej aplikacji wylistować pody działające w klasterze, tak aby zadziałał cluster bootstrap

https://github.com/bartosz822/lagom-k8s/blob/master/deploy/rbac.yml


**hello.yml**
W pliku hello.yml znajduje się konfiguracja deploymentu oraz serwisu z aplikacją hello.

https://github.com/bartosz822/lagom-k8s/blob/master/deploy/hello.yml


W pliku tym podmienić należy `PROJECT_ID` na id projektu nad którym się pracuje. W pliku zmienić można liczbę replik serwisu, domyślnie ustawione są 3.

**hello-stream.yml**
W pliku hello.yml znajduje się konfiguracja deploymentu oraz serwisu z aplikacją hello-stream.

https://github.com/bartosz822/lagom-k8s/blob/master/deploy/hello-stream.yml


W pliku tym podmienić należy `PROJECT_ID` na id projektu nad którym się pracuje. W pliku zmienić można liczbę replik serwisu, domyślnie ustawione są 3.

**zookeeper.yml**
W pliku zookeeper.yml znajduje się prosta konfiguracja zookeepera niezbędnego do poprawnego działania kafki.

https://github.com/bartosz822/lagom-k8s/blob/master/deploy/zookeeper.yml


**kafka.yml**
W pliku kafka.yml znajduje się prosta konfiguracja deploymentu i serwisu jednowęzłowej kafki.

https://github.com/bartosz822/lagom-k8s/blob/master/deploy/kafka.yml


**cassandra.yml**
W pliku cassandra.yml znajduje się prosta konfiguracja deploymentu i serwisu jednowęzłowej cassandry.

https://github.com/bartosz822/lagom-k8s/blob/master/deploy/cassandra.yml


**ingress.yml**
W pliku ingress.yml znajduje się konfiguracja load balancera, do którego przypisany będzie zewnętrzny adres ip, który będzie wejściem do naszej aplikacji.

https://github.com/bartosz822/lagom-k8s/blob/master/deploy/ingress.yml

## Deployment

W celu uruchomienia aplikacji na klastrze należy po kolei utworzyć kolejne obciążenia i serwisy za pomocą narzędzia `kubectl`.

`kubectl apply -f deploy/rbac.yml`
`kubectl apply -f deploy/cassandra.yml`
`kubectl apply -f deploy/zookeeper.yml`
`kubectl apply -f deploy/kafka.yml`
`kubectl apply -f deploy/hello.yml`
`kubectl apply -f deploy/hello-stream.yml`
`kubectl apply -f deploy/kafka.yml`

Po każdym wywołaniu `kubectl` zaczekaj na utworzenie odpowiednich obiektów w klastrze.

Po wykonanu komend przejdź do zakładki `Kubernetes Engine` → `Obciążenia` i sprawdź czy stworzyły się wszystkie odpowiednie pody:

![](https://paper-attachments.dropbox.com/s_993353410A63174E27B5DFE028DF6A2B837D353ADF11471BBD5D0FB1CFE9D6A5_1578829275492_image.png)



## Testowanie działania aplikacji
1. Testowanie aplikacji `hello`

Przejdź do zakładki `Usługi i ruch przychodzący` i skopiuj publiczny adres ip usługi `hello-ingress`.

![](https://paper-attachments.dropbox.com/s_993353410A63174E27B5DFE028DF6A2B837D353ADF11471BBD5D0FB1CFE9D6A5_1578831146428_image.png)


Otwórz przeglądarkę i wpisz adres `http://EXTERNAL_IP/api/hello/world`. W przeglądarce powinien wyświetlić się napis hello-world. 


2. Testowanie aplikacji `hello-stream`

Wejdź na stronę https://www.websocket.org/echo.html i w polu location wpisz: `ws://EXTERNAL_IP/stream` i  kliknij connect. Nastepnie w polu Message wpisz swoją wiadomomość i przetestuj działanie websocketów.


3. Testowanie współpracy 

Użyj narzędzia  `cURL` i wyślij zapytanie `POST` zmieniające wiadomość.

    curl -X POST \
      http://EXTERNAL_IP/api/hello/world \
      -H 'Cache-Control: no-cache' \
      -H 'Content-Type: application/json' \
      -d '{
            "message": "NEW_MSG"
    }'

Sprawdź czy wiadomość została zmieniona.
