% 99340 Tomas Duarte Fonseca Marques
:-[codigo_comum].
                                            % check_soma (lista de lista de inteiros(combinacoes), inteiro,lista de lista de inteiros(combinacoes)(output))
check_soma(Combs,Soma,Res):-                % eh uma funcao auxiliar a combinacoes_soma , funciona como um filtro , que recebe todas
    setof(X,(member(X,Res),sum_list(X,Soma)),Combs).  % as combinacoes , e retorna apenas as que tem uma soma de elemento X

combinacoes_soma(N,Els,Soma,Combs):-         % combinacoes_soma (Inteiro,Lista de inteiros, Inteiro,lista de listas de inteiros(output) )
    setof(Comb,combinacao(N,Els,Comb),Res),
    check_soma(Combs,Soma,Res).

n_primeiros(N,List,Res):-           % n_primeiros(inteiro, lista que vai cortar, lista resultante (output))
    length(Res,N),                  % eh um funcao auxiliar que recebe uma lista 1 , e retorna uma lista 2 com os N primeiros elementos da lista 1
    append(Res,_,List).


n_ultimos(N,List,Res):-             % n_ultimos(inteiro,lista que vai cortar,lista resultante (output))
    length(Res,N),                  % eh uma funcao auxiliar que recebe uma lista 1 , e retorna uma lista 2 com os n ultimos elementos da lista 1
    append(_,Res,List).

permutacoes_soma(N,Els,Soma,Perms):-        % permutacoes_soma(inteiro,lista de inteiros, inteiro,lista de listas(output))
    combinacoes_soma(N,Els,Soma,Combs),                 % utiliza o combinacoes_soma para tornar o predicado mais rapido e eficiente
    findall(X,(member(C,Combs),permutation(C,X)),Res),      % apenas vai correr todas as combinacoes , que tiverem a soma dada , e vai fazer permutacoes delas
    sort(Res,Perms).

sao_duas_listas(P,S):-          % sao_duas_listas(Algo,Algo) -> Booleano
    is_list(P),                 % esta funcao eh uma funcao auxiliar , que recebe dois argumentos ,e verifica se ambos sao listas
    is_list(S).

remove_lista(Fila,Esp):-            % remove_lista(Lista 1 , Lista 2(output))
    remove_lista_aux(Fila,Esp,[]).      %esta eh uma funcao auxiliar , que vai remover possiveis listas consecutivas ,
                                        % para facilitar o predicado espaco_fila
remove_lista_aux([],Esp,Esp).

remove_lista_aux([P,S|R],Esp,Aux):-         
    length([P,S|R],N),
    N >= 2,                 % caso tenhamos pelo menos dois elementos , verificamos se ambos sao listas caso sejam 
    sao_duas_listas(P,S),           % nao iremos guardar a primeira lista , e chamamos de novo a recursao
    remove_lista_aux([S|R],Esp,Aux).

remove_lista_aux([P,S|R],Esp,Aux):-
    length([P,S|R],N),
    N >= 2,
    \+sao_duas_listas(P,S),     % caso tenhamos pelo menos 2 elementos , mas nao sejam os dois listas ,entao podemos guardar o primeiro 
    append(Aux,[P],New_Aux),    % elemento e chamar a recursao
    remove_lista_aux([S|R],Esp,New_Aux).

remove_lista_aux([P|R],Esp,Aux):-
    length([P|R],N),
    N =:=1,                     % caso so tenhamos apenas 1 elemento , ja nao ha risco de guardarmos duas listas seguidas , 
    append(Aux,[P],New_Aux),    % entao apenas guardamos o elementos com append
    remove_lista_aux(R,Esp,New_Aux).

e_prefixo(A):-              % e_prefixo(argumento) -> booleano
    last(A,X),              % auxiliar ao predicado espaco_fila , a e_prefixo recebe um argumento , e retorna um booleano , caso correponda
    nonvar(X).              % ou nao a um prefixo(ultimo elemento nao pode ser var)

nao_e_sufixo(L):-               % e_sufixo(argumento)-> booleano 
    L \== [],               % auxiliar ao predicado espaco_fila , retorna True , caso L nao seja um sufixo , e False caso seja
    nao_e_sufixo_aux(L).        % L eh um sufixo caso seja uma lista vazia ou caso o primeiro elemento nao seja var

nao_e_sufixo_aux([P|_]):-       % verifica se o primeiro elemento eh var
    var(P).

tudo_var([]).

tudo_var([P|R]):-               % tudo_var(L) -> Booleano
    var(P),                     % recebe uma lista e retorna True caso seja uma lista de Variavei ,e False caso tenha algum elemento que 
    tudo_var(R).                % nao seja uma variavel

retira_numero(L,N,v):-          % retira o numero correspondente a soma vertical
    last(L,[P,_]),
    N = P.

retira_numero(L,N,h):-          % retira o numero correspondente a soma horizontal
    last(L,[_,R]),
    N = R.


espaco_fila(Fila,Esp,Ato):-             % espaco_fila(Lista, Esp(output),Atomo)
    remove_lista(Fila,New_Fila),            %comecaoms por remover as possiveis listas consecutivas
    append([Pref,New_Esp,Suf],New_Fila),    % e dps fazemos a parte principal do predicado , que eh isolar o Esp , com as devidas
    e_prefixo(Pref),                        % restricoes do sufixo e prefixo , definidas anteriormente
    \+nao_e_sufixo(Suf),
    tudo_var(New_Esp),
    New_Esp \== [],
    retira_numero(Pref,N,Ato),          % por fim retiramos o valor da soma , consoante o atomo , e unificamos o Esp com o espaco(N,New_Esp)
    Esp = espaco(N,New_Esp).

tudo_lista([]).

tudo_lista([P|R]):-             % tudo_lista(argumento) -> booleano
    is_list(P),                 % funcao auxiliar que recebe um argumento , e retorna true , caso seja uma lista de listas 
    tudo_lista(R).

espacos_fila(_,Fila,[]):-
    tudo_lista(Fila).

espacos_fila(Ato,Fila,Espacos):-                                  % espacos_fila(Atomo,Lista,Lista(output))
    \+tudo_lista(Fila), % caso se verifique eh porque existem espacos na fila
    bagof(X,espaco_fila(Fila,X,Ato),Espacos).                     % iremos guardar em Espacos todos os espacos encontrados na fila , 

eh_vazia(L):-        % funcao auxiliar , retorna True , caso L ,seja uma lista vazia [], False caso nao seja
    L == [].

espacos_puzzle(Puzzle,Espacos):-            % espacos_puzzle(matriz,lista de espacos)
    maplist(espacos_fila(h),Puzzle,Esp_h),       % executamos um maplist a as filas horizontais do Puzzle 
    mat_transposta(Puzzle,Puzzle_T),            % depois utilizamos a mat_transporta ,para transformarmos as colunas em linhas
    maplist(espacos_fila(v),Puzzle_T,Esp_v),    %  executaoms um maplist a Puzzle_T ,vai corresponder aos espacos verticais de Puzzle
    append(Esp_h,Esp_v,Esp),        % por fim juntamos os espacos horizontais com os espacos verticais
    append(Esp,Espacos).

comum_lista([P|_],L2):-          % retorna True caso existam elementos em comum entre as duas listas passadas como argumento
    pertence(P,L2),!.

comum_lista([P|R],L2):-
    \+pertence(P,L2),
    comum_lista(R,L2).

pertence(A,L1):-  % diz se A eh um elemento da lista L1
    member(B,L1), B == A.

check_esp_com(espaco(_,L_Pos1),espaco(_,L_Pos2)):-  % verifica se dois espacos , tem posicoes em comum 
    comum_lista(L_Pos1,L_Pos2).

espacos_com_posicoes_comuns(Espacos,Esp_1,Esp_com):-    % espacos_com_posicoes_comuns(Lista de Espacos,espaco,Lista de espacos(output))
    bagof(X,(member(X,Espacos), X \== Esp_1),New_Espaco),   % vai retirar o Esp_1 da lista de Espacos
    bagof(X,(member(X,New_Espaco),check_esp_com(Esp_1,X)),Esp_com). % de todos os membros de New_Espaco(Espacos sem Esp_1), vemos quais
                                                % tem posicoes em comum com Esp_1 , e guardamos em Esp_com

get_perms(espaco(Soma,Pos),Perms):-      % funcao auxiliar , tem como objetivo receber um argumento da forma espaco(Soma,Pos), e 
    length(Pos,N),                  % retornar uma lista de permutacoes ,correspondetes aos criterios(tamanho e soma) desse espaco
    permutacoes_soma(N,[1,2,3,4,5,6,7,8,9],Soma,Perms).

permutacoes_soma_espacos(Espacos,Perm_soma):-           % permutacoes_soma_espacos(Lista de espacos,lista de listas(output))
    bagof([Esp,Perms],(member(Esp,Espacos),get_perms(Esp,Perms)),Perm_soma).    % por cada membro de Espacos , vamos retirar as Perms
                                                                    % e guardamos o respetivo espaco e permutacoes na forma [espaco,perms] 
                                                                    % dentro do Perm_soma
pertence_sublista(E,L):- % E pertence a sublista L 
    append(L,New_L),
    pertence(E,New_L),!.

transforma_perm(Perm_L,Perm):-      % funcao que recebe uma lista de permutacoes , e retorna uma lista com todos os numeros que aparecem
    findall(Aux_Perm,(member(X,Perm_L),member(Aux_Perm,X)),A_Perm), % nessas permutacoes
    sort(A_Perm,Perm).

pertence_perm([],[]).

pertence_perm([P|R],[P1|R1]):-
    pertence(P,P1),
    pertence_perm(R,R1).

permutacao_possivel_espaco(Perm,Esp,Espacos,_):-
    bagof(X,(member(X,Espacos), X \== Esp),New_Espacos), % fazemos uma lista de espacos sem o Esp
    get_perms(Esp,Perm_esp),    % retiramos as permutacoes para o Esp
    espacos_com_posicoes_comuns(New_Espacos,Esp,Esp_com),   % vemos os espacos com posicoes comuns a Esp
    maplist(get_perms,Esp_com,Perm__pos_esp),   % guardamos as permutacoes desses espacos, comuns
    maplist(transforma_perm,Perm__pos_esp,Perm_Aux),    % e utilizamos a transforma_perm , para vermos que numeros aparecem podem ter as permutacoes dos espacos com posicoes comuns
    findall(X,(member(X,Perm_esp),pertence_perm(X,Perm_Aux)),Perm_res), % e vemos que permutacoes do Esp sao possiveis , isto eh , se estao
    member(Perm,Perm_res).                      % presentes nas permutacoes dos espacos com posicoes comuns

retira_var(espaco(_,V),R):- % funcao auxiliar que recebe um argumento na forma espaco(Soma,P), e retorna o valor de P
    R = V.

permutacoes_possiveis_espaco(Espacos,Perm_soma,Esp,Perm_poss):-         % permutacoes_possiveis_espaco(Lista de espacos,lista com permutacoes e espacos,espaco,lista correspondente ao espaco)
    bagof(X,permutacao_possivel_espaco(X,Esp,Espacos,Perm_soma),Perms), % vemos as permutacoes possiveis para o espaco dado
    retira_var(Esp,V),               % retiramos as posicoes do espaco , e por fim unificamos Perm-Poss com uma lista que contem as posicoes
    Perm_poss = [V,Perms].          % e as possiveis permutacoes

permutacoes_possiveis_espacos(Espacos,Perms_poss_esps):-    % permutacoes_possiveis_espacos(lista de espacos, lista de permutacoes possiveis)
    permutacoes_soma_espacos(Espacos,Perm_soma),    % neste predicado , apenas executamos o permutacoes_possiveis_espaco, para todos os espacos do Puzzle
    bagof(X,(Esp,X)^(member(Esp,Espacos),permutacoes_possiveis_espaco(Espacos,Perm_soma,Esp,X)),Perms_poss_esps).

remove_primeiro([_|R],R). % R , corresponde a a remocao do primeiro elemento da lista

tudo_igual(L):-     % funcao auxiliar que retorna True , caso os elementos da lista sao todos iguais
    sort(L,L1),
    length(L1,1).

numeros_comuns(Sub_L,Numeros_Comuns):-  % numeros_comuns(Lista de permutacoes, lista de tuplos (indice,elemento))
    mat_transposta(Sub_L,L_Aux),        % utilizamos a mat_transporta para organizar os elementos de Sub_L , por indices, assim os elementos
    maplist(sort,L_Aux,Aux),    % de indice 1 , ficam todos na primeira lista , os de indice 2 na segund , etc
    findall((N,P),(member([P|R],Aux),length([P|R],1),nth1(N,Aux,[P|R])),Res),   % o maplist(sort), vai cortar os duplicados , pelo que forem todos iguais , a lista vai ter comprimento 1
    sort(Res,Numeros_Comuns). % no findall , vamos guardar o indice e o elemento de uma possivel lista de comprimento 1, e no fim ordenamos , para ficar tudo em ordem


unifica([P,R]):-   % recebe uma lista do tipo [Posicoes do espaco, Numeros comuns ] , e faz as respetivas unificacoes
    uni_aux(P,R).

uni_aux(_,[]). % auxiliar da unifica

uni_aux(Esp,[(I,N)|R]):-
    unifica_var(Esp,I,N),
    uni_aux(Esp,R).

unifica_var(Esp,I,N):- % auxiliar da unifica
    nth1(I,Esp,N).

atribui_comuns(Perms_Poss):- % atribui_comuns(Lista de Permutacoes possiveis)
    bagof([P,Num_Com],(P,R,Num_Com)^(member([P,R],Perms_Poss),numeros_comuns(R,Num_Com)),Subst), % verifica se existem numeros comuns nas permutacoes
    maplist(unifica,Subst).     % caso haja numeros comuns , usamos o maplist(unifica), para fazer as respetivas unificacoes

retira_impossiveis_aux(P,Perm,New_Perm):- % De todas as permutacoes associadas ao espaco , vai ver quais eh que unificam e guarda apenas essas
    bagof(X,(P,X)^(member(X,Perm), X = P),New_Perm).

retira_impossiveis(Perms,Novas_Perms):- % retira_impossiveis(Lista de permutacoes possiveis, Lista de permutacoes apos alteracoes)
    bagof([Pos,New_Perm],(Per,New_Perm)^(member([Pos,Per],Perms),retira_impossiveis_aux(Pos,Per,New_Perm)),Novas_Perms).
% vamos utilizar um bagof pra executar o retira_impossiveis_aux a todos os elementos de Perms

check_paragem(Input,Output):-   % caso nao haja diferencas apos um atribui_comuns , eh porque o puzzle e impossivel ou acabou , entao para
    atribui_comuns(Input),
    Input == Output.

simplifica(Perms,Novas_Perms):- % simplifica(Lista de permutacoes possiveis,simplificacao de Perms)
    atribui_comuns(Perms),      % vamos atribuir os comuns a Perms
    retira_impossiveis(Perms,Novas_Perms),  % retiramos as permutacoes impossiveis
    \+check_paragem(Novas_Perms,Novas_Perms),   % e vemos se check_paragem retorna False (ainda eh possivel continuar)
    retira_impossiveis(Novas_Perms,N_Perms), % entao retiramos os impossiveis apos a alteracao no check_paragem
    simplifica(N_Perms,_). % e chamamos a recursao 

simplifica(Perms,Novas_Perms):- 
    atribui_comuns(Perms),
    retira_impossiveis(Perms,N_Perms),
    check_paragem(N_Perms,N_Perms), % aqui o check_paragem retorna True , entao paramos retiramos os impossives e paramos
    retira_impossiveis(N_Perms,Novas_Perms).

inicializa(Puzzle,Perms):-  % inicializa(Matriz,Lista de listas) 
    espacos_puzzle(Puzzle,Espacos), % na inicializa , chamamos os predicados necessarios , para chamarmos o simplifica
    permutacoes_possiveis_espacos(Espacos, Perms_Poss),
    simplifica(Perms_Poss,Perms).

menor(Perms,Menor):-               % ve qual o menor comprimento de uma lista de permutacoes
    findall(N,(member([_,Per],Perms),length(Per,N), N =\= 1),Comps),
    sort(Comps,[Menor|_]). % ao utilizarmos o sort , fica por ordem , e o primeiro elemento vai ser o menor

check_comp([_,R],N):-   % retorna o numero de permutacoes possiveis que existem
    length(R,N).

escolhe_menos_alternativas([P|R],Escolha):- % escolhe_menos_alternativas(lista de permutacoes possiveis,elemento da lista passada como argumento)
    menor([P|R],Menor), % unifica Menor , com o menor numero de permutacoes possiveis >= 2
    check_comp(P,N),    % verica quantas Perms , tem P
    N =\= Menor,    % caso nao seja o menor numero , chamamos a recursao de novo e analisamos a proxima permutacao
    escolhe_menos_alternativas(R,Escolha).

escolhe_menos_alternativas([P|R],P):-
    menor([P|R],Menor),
    check_comp(P,N),    
    N =:= Menor.    % neste caso , o comprimento de P eh igual ao menor , entao paramos e unificamos a Escolha com P

check_escolha(Esp,Esc):- 
    Esp == Esc.

substitui_escolha(Esc,Subst, L1, L2) :-     % funcao de substituicao, auxiliar do experimenta_perm
    maplist(substitui_escolha_aux(Esc, Subst), L1, L2). % executamos um maplist , e assim vamos fazer a substituicao em todos os elementos de L1

  substitui_escolha_aux(Esc, Subst, El, Subst) :-
    check_escolha(Esc,El). % verifica se o El unifica com a Escolha, caso unifica , substituimos o El por Subst

  substitui_escolha_aux(Esc, _, El, El) :-
    \+check_escolha(Esc,El). % canso El nao unifique com a Escolha , caso nao unifica , continua como estava

experimenta_perm([Pos,Perms],Perms_Poss,Novas_Perms_Poss):- % experimenta_perm(Escolha,Lista de permutacoes possiveis,Perms_Poss apos as alteracoes)
    member(P,Perms), % por cada membro das Permutacoes da Escolha
    Pos = P, 
    Res = [Pos,[P]], % fazemos a unificacao desejada , e unificamos Res = [Po,[P]]
    substitui_escolha([Pos,Perms],Res,Perms_Poss,Novas_Perms_Poss). % e no fim vamos substituir a nossa escolha no Perms_Poss por Res

resolve_aux_aux(Perms_Poss,Novas_Perms):- 
    \+escolhe_menos_alternativas(Perms_Poss,_), % caso escolhe_menos_alternativas retorne False , eh porque o Puzzle ja esta resolvido
    simplifica(Perms_Poss,Novas_Perms). % entao apenas simplificamos as Permutacoes e guarda em Novas_Perms

resolve_aux_aux(Perms_Poss,Novas_Perms):- % caso o escolhe_menos_alternativas nao retorne falso , vamos seguir a ordem do algoritmo da seccao 2.3
    escolhe_menos_alternativas(Perms_Poss,Escolha),
    experimenta_perm(Escolha,Perms_Poss,Nova_Perms),
    simplifica(Nova_Perms,N_Perms),
    resolve_aux_aux(N_Perms,Novas_Perms). % e no fim chamamos a recursao de novo

resolve_aux(Perms_Poss,Novas_Perms):- % resolve_aux(lista de permutacoes possiveis , resultado do puzzle)
    length(Perms_Poss,N), % unifica N com o quantidade de espacos
    bagof(X,(resolve_aux_aux(Perms_Poss,X),length(X,N)),N_Perms), % utilizamos o bagof e fazemos o resolve_aux_aux, e caso o comprimento de X seja N , entao guardamos o resultado
    append(N_Perms,Novas_Perms).

resolve(Puzzle):- % resolve(Puzzle) -> Puzzle resolvido
    inicializa(Puzzle,Perms), % comecamos por inicalizar o Puzzle , e guardamos em Perms , o Puzzle com as alteracoes feitas
    resolve_aux(Perms,_). % depois apenas obtemos a solucao do Puzzle com o resolve_aux ,e fazemos as unificacoes necessarias