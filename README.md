# SpeechActMiner
Finding Speech Act in Events Logs

Propose: Using Natural Language Process - Stanford Java, Speech Act Theory and Linguistic Perspective, it was possible extract kinds of speech act are considered making decisions. This application was build using a dataset of a company which provides services of infraestructure. This dataset has exchange mensages among clients and employees.

Metodology:

Cleaning informations: It was done a cleaning of every informations were not considered importants to miner points of decision and mensages that happing before of these points, as telfephone numbers, emails, address, especial characters, etc;.

Mining Making Decision : Through of tecniques investigation for discovering proposes business rule in paper Campos et al. (2017) and group of verbs referring a making decision proposed by Gunnarsson, M. (2006),it was built code for extrating all points of making decision among mensages (already mentioned before), using functions and libs of Stanford NLP [Reese, 2005].

Mining Speech Acts Featuring Decisions
After to find making decisions, it was listed all mensages which precede these decisions. In these mensages, there was more than one kind of speech act. For having more precision in results, it was find the main speech act of each mensage, using rules avaliable in Speech Act Theory. Thus, it was possible to identify the most found speech act which can feature making decision.

Plataform: Java 1.8 Maven Netbeans IDE 8.2

Procedure for application:

Clone this application for PC c:\ (Windows), \home (Linux), etc.;
Add file jar "opennl-tools-1.8.4.jar" in project libraries. This file is at /speechact/lib. Click in "Build Clear Project" button;
Verify if the file paths is according to where you cloned this application;
There is a file with examples of events log. (speechact/arquivos/LogMessage.csv).This file was built for you test this application.
The main file is AtosDeFalaDecisao.java. You need execute this application starting from this file.
Updated plataform: Java 1.8 Maven IDE free

Clone this application
build: mvn clean install
From any terminal with maven in path you can run: java -cp target/speechact-0.0.1-SNAPSHOT.jar:<>/.m2/repository/jaws/jaws-bin/1.0.0/jaws-bin-1.0.0.jar:<>/.m2/repository/opennlp/opennlp-tools/1.8.4/opennlp-tools-1.8.4.jar:<>/.m2/repository/commons-io/commons-io/2.6/commons-io-2.6.jar:. decisaocomatosdefala.execucao.AtosDeFalaDecisao
Obs.: Change <> to your user path like /Users/edveloso

References:

[1] Campos, J., Richetti,P., Baião, F.A., Santoro, F.M.:"Discovering business rules in Knowledge Intensive Process through decision mining experimental study." In: 5th International Workshop on Declarative/Decision/Hybrid Mining & Modeling for Business Processes (DeHMiMoP’17). [2017]

[2] Bach,K., Harnish, R.M.: "Linguistic Communication and speech acts." In: The MIT Press – Cambridge (2007)

[3] Gunnarsson,M: "Group Decision-Making – Language and Interaction." In: Therése Foleby -ISBN: 91-975752-6-7 (2006)

[4] Reese, R.M.: "Natural Language Processing with Java." In: Packt Publishing . Avaliable in: www.it-ebook.info. . Accessed in: 10/09/2017.
