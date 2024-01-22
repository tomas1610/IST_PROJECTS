Este projeto é um jogo da forca.
Compilar a aplicacão:
deverá dar make no terminal , isto irá compilar o player e o servidor.

Correr o player:
deverá fazer ./player [-n IP] [-p PORT] , em que o IP e o PORT são argumentos opcionais , caso o utilizador não os passe , o servidor deverá correr localmente e/ou no porto 58038

Correr o server:
./GS path_file [-p PORT] [-v] , em que path_file é o caminho para o nosso ficheiro de palavras, PORT é o porto onde queremos correr o nosso servidor , e -v é uma flag para verbose mode , isto é escrever uma descrição de todos os pedidos


Comandos:
start PLID ou sg PLID (inicia o jogo em que PLID é um numero de aluno)
play letter ou pl letter( faz uma jogada de uma letra ) 
guess word  ou gw word( tenta adivinhar a palavra , deverá enviar um palavra válida)
quit (termina o jogo do jogador atual)
exit(termina o jogo se este estiver ativo , e termina a aplicação do player)
scoreboard ou sb( escreve no terminal o top 10 de jogadores do servidor em que estamos a jogar e cria tambem um ficheiro )
state ou st ( escreve no terminal o estado de um jogo ativo , caso exista , ou mostra o resumo do ultimo jogo jogado, cria tambem um ficheiro .txt com esse conteudo)
hint ou h (faz dowload de uma imagem que serve como dica para a palavra , esta imagem estara disponivel na diretoria onde correu o player)