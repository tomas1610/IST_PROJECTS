#Tomas Duarte Fonseca Marques ist199340

# tabueliro -> logico
def eh_tabuleiro(tab):
    '''esta funcao recebe um tabuleiro e com ajuda da funcao eh_linha , vai 
    retornar True , caso seja tabuleiro , ou False caso nao seja'''
    if not (isinstance(tab, tuple) and len(tab) == 3):
        return False
    for l in tab:
        if not eh_linha(l):
            return False
        
    return True
# linha -> logico 
def eh_linha(l):
    '''esta funcao auxiliar da eh_tabuleiro , recebe os elementos de tab , ou 
    seja 3 tuplos e diz se correspondem a linhas do tabuleiro , isto com a ajuda
    da funcao eh_elemento'''
    if not (isinstance(l, tuple) and len(l) == 3):
        return False
    for n in l:
        if not eh_elemento(n):
            return False
        
    return True
# elemento -> logico      
def eh_elemento(n):
    ''' esta funcao complementar da eh_linha , recebe os elementos de cada linha
    e devolve True , caso sejam elementos , isto e se forem numeros inteiros 
    entre -1 e 1 , e devolve False caso nao sejam'''
    if ((type(n)== int) and ( n>= -1) and (n <= 1)):
        return True
    else:
        return False
# posicao -> logico
def eh_posicao(num):
    '''recebe uma posicao e devolve True, caso seja uma posicao do tabuleiro 
    , isto e se for um numero inteiro entre 1 e 9 ,ou False caso nao seja '''
    if ((type(num)==int) and (num >= 1) and (num <= 9)):
        return True
    else:
        return False
# tabuleiro x inteiro -> vetor
def obter_linha(tab,c):
    '''esta funcao recebe um tabuleiro e um inteiro , correspondente a linha que
    queremos obter , caso seja inserido algo que nao seja (tabuleiro,inteiro),
    a funcao gera um erro , senao ela devolve a linha desejada'''
    if not (eh_tabuleiro(tab) and (type(c)== int) and (c>=1) and (c<=3)) :
        raise ValueError('obter_linha: algum dos argumentos e invalido')
    else:
        return tab[c-1]
# tabuleiro x inteiro -> vetor
def obter_coluna(tab, c):
    '''esta funcao recebe um tabuleiro e um inteiro , correspondente a coluna
    que queremos obter , caso seja inserido algo que nao seja (tabuleiro,inteiro),
    a funcao gera um erro , senao ela devolve a coluna desejada'''
    if not (eh_tabuleiro(tab) and (type(c)==int) and (c>=1) and (c<=3)):
        raise ValueError('obter_coluna: algum dos argumentos e invalido')
    else:
        col = ()
        for i in range (0,3):
            m = tab[i]
            n = m[c-1]
            col = col + (n,)
        return col        
# tabuleiro x inteiro -> vetor
def obter_diagonal(tab,c):
    '''a funcao recebe um tabuleiro e um inteiro que pode ser 1 ou 2, caso seja 
    1, a funcao devolve a diagonal descendente da esquerda para a direita , caso
    seja 2, devolve a diagonal ascendente da esquerda para a direita, se for
    inserido algo que nao seja um tabuleiro , ou um numero diferente de 1 ou 2,
    a funcao gera erro '''
    if not(eh_tabuleiro(tab) and type(c)==int and ((c == 1) or (c == 2))):
        raise ValueError('obter_diagonal: algum dos argumentos e invalido')
    else:
        diagonal = ()
        if c == 1:
            for i in range(0,3):
                m = tab[i]
                n = m[i]
                diagonal = diagonal + (n,) 
                
        if c == 2:
            for i in range(1,4):
                m = tab[-i]
                n = m[i-1]
                diagonal = diagonal + (n,)
        return diagonal
# tabuleiro -> cadeia de carateres    
def tabuleiro_str(tab):
    ''' a funcao recebe o tabuleiro e devolve a cadeia de carateres
    correspondente ao tabuleiro, tornando os 1 em X e os -1 em O , se for 0 nao 
    escreve nada'''
    if not (eh_tabuleiro(tab)):
        raise ValueError('tabuleiro_str: o argumento e invalido')
    else:
        rep_t = ''
        for linha in tab:
            rep_t = rep_t + escreve_linha(linha)
        return rep_t[:-len('------------\n')]
# linha -> cadeia de carateres
def escreve_linha(l):
    '''esta funcao recebe uma linha e devolve a cadeia de carateres 
    correspondente a linha , e uma funcao auxiliar da tabuleiro_str'''
    linha = ''
    for e in l:
        if e == 1:
            (e) = (' X')
        if e == -1:
            (e) = (' O')
        if e == 0:
            (e) = ('  ')
        linha = linha + str(e) + ' | ' 
        linha = linha[:-1]
    return (linha[:-1]) + '\n-----------\n' 
# tabuleiro x posicao -> logico
def eh_posicao_livre(tab,c):
    '''esta funcao recebe um tabuleiro e uma posicao e devolve True caso a
    posicao esteja livre, ou False caso nao esteja, se for inserido um valor que
    nao seja tabuleiro ou um posicao que nao seja posicao , entao a funcao da
    erro '''
    if not (eh_tabuleiro(tab) and eh_posicao(c)):
        raise ValueError('eh_posicao_livre: algum dos argumentos e invalido')
    else:
        if ler_posicao(tab,c) == 0 :
            return True
        else:
            return False
# tabuleiro x posicao -> inteiro        
def ler_posicao(tab,pos):
    ''' esta funcao recebe um tabuleiro e uma posicao, e devolve o valor dessa
    posicao, e uma funcao auxiliar da eh_posicao_livre '''
    if (pos > 6) :
        linha = tab[2]
        valor_posicao = linha[pos - 7]
    elif (pos > 3) :
        linha = tab[1]
        valor_posicao = linha[pos - 4]
    elif (pos <= 3): 
        linha = tab[0]
        valor_posicao = linha[pos - 1]
    return valor_posicao

# tabuleiro -> vetor
def obter_posicoes_livres(tab):
    ''' esta funcao recebe um tabuleiro e devolve todas a posicoes que se
    encontram livres , caso o tabuleiro inserido nao corresponda a um tabuleiro
    ela gera um erro'''
    if not eh_tabuleiro(tab):
        raise ValueError('obter_posicoes_livres: o argumento e invalido')
    else:
        livres = ()
        tuplo = ()
        for m in tab:
            for e in m:
                tuplo = tuplo +(e,)
        for e in range(0,9):
            n = tuplo[e]
            if n == 0:
                livres = livres + (e + 1,)
        return livres
# tabuleiro -> inteiro
def jogador_ganhador(tab):
    '''esta funcao recebe um tabuleiro e gera um erro , caso nao seja um
    tabuleiro, caso seja tabuleiro devolve o 1 , se tiver ganho o jogador 1 ,
    -1 para o caso de vitoria do jogador -1 , ou entao devolve 0 , se ninguem
    tiver ganho '''
    if not eh_tabuleiro(tab):
        raise ValueError('jogador_ganhador: o argumento e invalido')
    else:
        if ( obter_coluna(tab,1) == (1,1,1) or obter_coluna(tab,2) == (1,1,1) or obter_coluna(tab,3) == (1,1,1)
          or obter_linha(tab,1) == (1,1,1)  or obter_linha(tab,2) == (1,1,1)  or obter_linha(tab,3) == (1,1,1) 
          or obter_diagonal(tab,1) == (1,1,1) or obter_diagonal(tab,2) == (1,1,1)):
            return 1
        if ( obter_coluna(tab,1) == (-1,-1,-1) or obter_coluna(tab,2) == (-1,-1,-1) or obter_coluna(tab,3) == (-1,-1,-1)
          or obter_linha(tab,1) == (-1,-1,-1)  or obter_linha(tab,2) == (-1,-1,-1)  or obter_linha(tab,3) == (-1,-1,-1) 
          or obter_diagonal(tab,1) == (-1,-1,-1) or obter_diagonal(tab,2) == (-1,-1,-1)):
            return -1       
        else:
            return 0 
# tabuleiro x posicao x inteiro -> tabuleiro        
def marcar_posicao(tab,jog,pos):
    '''esta funcao recebe um tabuleiro , um inteiro correspondete ao jogador
    e uma posicao onde queremos jogar , caso algum dos argumentos seja invalido,
    a funcao gera um erro , senao ela devolve o tabuleiro alterado'''
    if not ((eh_tabuleiro(tab))and type(jog)== int and ((jog==1)or(jog==-1)) and eh_posicao(pos)):
        raise ValueError('marcar_posicao: algum dos argumentos e invalido')
    else: 
        if not eh_posicao_livre(tab,pos):
            raise ValueError('marcar_posicao: algum dos argumentos e invalido')
        else:
            if pos <= 3:
                linha = obter_linha(tab,1)
                l = list(linha)
                l[pos-1] = jog
                alterado = tuple(l)
                tabuleiro_l = list(tab)
                tabuleiro_l[0] = alterado
                tab = tuple(tabuleiro_l)
            if (pos > 3) and (pos <= 6):
                linha = obter_linha(tab,2)
                l = list(linha)
                l[pos-4] = jog
                alterado = tuple(l)
                tabuleiro_l = list(tab)
                tabuleiro_l[1] = alterado
                tab = tuple(tabuleiro_l)
            if (pos > 6) and (pos <= 9):
                linha = obter_linha(tab,3)
                l = list(linha)
                l[pos-7] = jog
                alterado = tuple(l)
                tabuleiro_l = list(tab)
                tabuleiro_l[2] = alterado
                tab = tuple(tabuleiro_l) 
            return tab  
        
# tabuleiro -> posicao
def escolher_posicao_manual(tab):
    '''esta funcao recebe um tabuleiro, caso nao seja um tabuleiro da erro, senao
    passa a proxima fase em que pede ao jogador que insira uma posicao livre
    se a posicao nao for uma posicao , nem for uma posicao livre , entao a funcao
    gera erro , se os argumentos inseridos estiverem todos corretos, por fim a 
    funcao devolve a posicao onde marcou'''
    if not(eh_tabuleiro(tab)):
        raise ValueError('escolher_posicao_manual: o argumento e invalido')
    pos = eval(input('Turno do jogador. Escolha uma posicao livre: '))
    if pos < 1 or pos > 9:
        raise ValueError('escolher_posicao_manual: a posicao introduzida e invalida')
    else:
        if not eh_posicao_livre(tab,pos):
            raise ValueError('escolher_posicao_manual: a posicao introduzida e invalida')
        else:
            return pos
            
# tabuleiro x inteiro x cadeia de carateres -> posicao
def escolher_posicao_auto(tab,jog,est):
    '''a funcao recebe como argumentos um tabuleiro , um inteiro correspondente
    ao jogador e uma cadeia de carateres correspondente a estrategia escolhida,
    caso algum dos argumentos seja invalido, a funcao da erro, senao devolvera 
    a posicao onde pode marcar, consoante a estrategia selecionada'''
    if not (eh_tabuleiro(tab) and type(jog)== int and ( jog == 1 or jog == -1)):
        raise ValueError('escolher_posicao_auto: algum dos argumentos e invalido')
    else:
        if est == 'basico':
            return basico(tab,jog)
        if est == 'normal':
            return normal(tab,jog)
        if est == 'perfeito':
            return perfeito(tab,jog)
        else:
            raise ValueError('escolher_posicao_auto: algum dos argumentos e invalido')
        
            
# tabuleiro x inteiro -> posicao        
def basico(tab,jog):
    '''esta funcao corresponde a estrategia basica, esta segue os criterios 
    5,7 e 8 , ou seja joga no centro se este estiver livre , senao joga no
    primeiro canto que encontrar livre , senao joga na primeira lateral que
    encontrar livre , por fim devolve a posicao em que quer marcar'''
    # ve se o centro esta livre, se estiver devolve a posicao 5 
    if eh_posicao_livre(tab,5):   
        return 5
    # ve qual o primeiro canto livre e devolve esse canto
    elif jogar_canto(tab,jog):
        return jogar_canto(tab,jog) 
    # ve qual a primeira lateral livre e devolve essa lateral
    elif jogar_lateral(tab,jog):
        return jogar_lateral(tab,jog)

#tabuleiro x inteiro -> posicao
def normal(tab,jog):
    ''' esta estrategia faz o mesmo que a basica , mas comeca por verificar se
    ha alguma jogada vencedora , em que possamos ganhar , senao vai ver se ha 
    alguma jogada vencedora para o adversario , caso nada disto acontece ve se 
    o centro esta livre e devolve o centro, senao estiver livre ele ve se o 
    adversario jogou em algum canto , se sim joga no canto oposto , senao vai
    ver qual o primeiro canto livre e se nao houver , ve as laterais'''
    # ve se existe uma jogada de vitoria
    if jogada_da_vitoria(tab,jog):
        return jogada_da_vitoria(tab,jog)
    # ve se existe uma jogada de vitoria para o adversario
    elif jogada_de_bloqueio(tab,jog):
        return jogada_de_bloqueio(tab,jog)
    # ve se o canto esta livre
    elif eh_posicao_livre(tab,5):      
        return 5
    # ve se o adversario jogou em algum canto, se sim devolve o canto oposto
    elif canto_oposto(tab,jog):
        return canto_oposto(tab,jog)
    # ve qual o primeiro canto livre
    elif jogar_canto(tab,jog):
        return jogar_canto(tab,jog)
    # ve qual a primeira lateral livre
    elif jogar_lateral(tab,jog):
        return jogar_lateral(tab,jog)
# tabuleiro x interio -> posicao
def perfeito(tab,jog): 
    '''recebe um tabuleiro e um inteiro correspondente ao jogador, faz o mesmo
    que a estregia normal, apenas com a diferenca que depois de ver se ha uma 
    jogada de vitoria para o adversario , vai ver se ha alguma possibilidade de
    bifurcacao, se houver devolve essa posicao, senao vai ver se ha alguma 
    possibilidade de bifurcacao para o adversario e defende'''
    # ve se tem uma jogada de vitoria
    if jogada_da_vitoria(tab,jog):
        return jogada_da_vitoria(tab,jog)
    # ve se existe uma possibilidade de vitoria para o adversario
    if jogada_de_bloqueio(tab,jog):
        return jogada_de_bloqueio(tab,jog) 
    # ve se existe uma bifurcacao para ele mesmo
    if atc_bifurcacao(tab,jog): 
        return atc_bifurcacao(tab,jog)  
    # ve se existe uma jogada de bifurcacao para o adversario
    if def_bifurcacao(tab,jog):
        return def_bifurcacao(tab,jog) 
    # ve se o canto esta livre
    if eh_posicao_livre(tab,5):
        return 5 
    # ve se o canto oposto esta livre
    if canto_oposto(tab,jog):
        return canto_oposto(tab,jog)
    # ve qual o primeiro canto livre que encontra
    if jogar_canto(tab,jog):
        return jogar_canto(tab,jog)
    # ve qual a primeira lateral livre que encontra
    if jogar_lateral(tab,jog):
        return jogar_lateral(tab,jog)  

# tabuleiro -> posicao            
def jogar_canto(tab,jog):
    '''recebe um tabuleiro e caso encontre algum canto livre devolve o primeiro
    que encontrar'''
    
    cantos_do_tabuleiro = [1, 3, 7, 9]
    for pos_canto in cantos_do_tabuleiro:
        if eh_posicao_livre(tab,pos_canto):
            return pos_canto

# tabuleiro -> posicao
def jogar_lateral(tab,jog):
    '''recebe um tabuleiro e vai ver se existem laterais livres , caso existam
    devolve a primeira lateral livre que encontrar'''
    laterais_do_tabuleiro = [2, 4, 6, 8]

    for pos_lateral in laterais_do_tabuleiro:
        if eh_posicao_livre(tab,pos_lateral):
            return pos_lateral    
        
# tabuleiro x inteiro -> posicao
def jogada_da_vitoria(tab,jog):
    '''recebe um tabuleiro e um inteiro correspondente ao jogador, e vai ver se
    existe alguma possibilidade de vitoria para esse jogador, caso exista ela 
    devolve essa mesma posicao , senao nao devolve nada'''
    if obter_linha(tab,1) == (0,jog,jog) or obter_coluna(tab,1) == (0,jog,jog) or obter_diagonal(tab,1)== (0,jog,jog):
        
        return 1
    elif obter_linha(tab,1) == (jog,0,jog) or   obter_coluna(tab,2) == (0,jog,jog):

        return 2
    elif obter_linha(tab,1) == (jog,jog,0) or obter_coluna(tab,3) == (0,jog,jog) or obter_diagonal(tab,2)== (jog,jog,0):
        
        return 3
    elif obter_linha(tab,2) == (0,jog,jog) or obter_coluna(tab,1) == (jog,0,jog):
     
        return 4

    elif obter_linha(tab,2) == (jog,jog,0) or obter_coluna(tab,3) == (jog,0,jog):
       
        return 6
    elif obter_linha(tab,3) == (0,jog,jog) or obter_coluna(tab,1) == (jog,jog,0) or obter_diagonal(tab,2)== (0,jog,jog):
    
        return 7
    elif obter_linha(tab,3) == (jog,0,jog) or obter_coluna(tab,2) == (jog,jog,0):
      
        return 8
    elif obter_linha(tab,3) == (jog,jog,0) or obter_coluna(tab,3) == (jog,jog,0) or obter_diagonal(tab,1)== (jog,jog,0):
      
        return 9 
# tabuleiro x jogador -> inteiro    
def jogada_de_bloqueio(tab,jog):
    '''recebe um tabuleiro e um inteiro correspondente ao jogador, e vai 
    averiguar se existe uma possibilidade de vitoria para o adversario, caso
    exista ele devolve essa posicao ,para depois jogarmos la '''
    return jogada_da_vitoria(tab,-jog)

# tabuleiro x inteiro -> posicao
def canto_oposto(tab,jog):
    '''recebe um tabuleiro e um inteiro corresponde ao jogador que somos, e vai
    ver se as diagonais , para ver se o adversario marcou num canto e caso tenha
    marco , jogamos no canto oposto e a funcao devolve a posicao onde iremos 
    jogar '''
    if obter_diagonal(tab,1)==(0,jog,-jog):
   
        return 1
    elif obter_diagonal(tab,2)==(-jog,jog,0):
    
        return 3
    elif obter_diagonal(tab,2)==(0,jog,-jog):
  
        return 7
    elif obter_diagonal(tab,1)==(-jog,jog,0):
     
        return 9
# tabuleiro x inteiro -> vetor       
def obter_bifurcacoes(tab,jog):
    '''a funcao recebe um tabuleiro e um inteiro , correspondente a um jogador,
    e devolve todas as posicoes , onde caso o jogador jogue faz uma bifurcacao'''
    bifurcacoes = ()
    # possiveis intersecoes na posicao 1
    if (jog in obter_linha(tab,1) and jog in obter_coluna(tab,1)):
        if (-jog not in obter_linha(tab,1) and -jog not in obter_coluna(tab,1)):
            if eh_posicao_livre(tab,1):
                bifurcacoes += (1,)
    elif (jog in obter_linha(tab,1)) and (jog in obter_diagonal(tab,1)):
        if (-jog not in obter_linha(tab,1) and -jog not in obter_diagonal(tab,1)):
            if eh_posicao_livre(tab,1):
                bifurcacoes += (1,)            
    elif (jog in obter_coluna(tab,1) and jog in obter_diagonal(tab,1)):
        if (-jog not in obter_coluna(tab,1) and -jog not in obter_diagonal(tab,1)):
            if eh_posicao_livre(tab,1):
                bifurcacoes += (1,)     
    # possiveis intersecoes na posicao 2
    if (jog in obter_linha(tab,1) and jog in obter_coluna(tab,2)):
        if (-jog not in obter_linha(tab,1) and -jog not in obter_coluna(tab,2)):
            if eh_posicao_livre(tab,2):
                bifurcacoes += (2,)         
    # possiveis intersecoes na posicao 3
    if (jog in obter_linha(tab,1) and jog in obter_coluna(tab,3)):
        if (-jog not in obter_linha(tab,1) and -jog not in obter_coluna(tab,3)):
            if eh_posicao_livre(tab,3):
                bifurcacoes += (3,)            
    elif (jog in obter_linha(tab,1)) and (jog in obter_diagonal(tab,2)):       
        if (-jog not in obter_linha(tab,1) and -jog not in obter_diagonal(tab,2)):
            if eh_posicao_livre(tab,3):
                bifurcacoes += (3,)            
    elif (jog in obter_coluna(tab,3) and jog in obter_diagonal(tab,2)):
        if (-jog not in obter_coluna(tab,3) and -jog not in obter_diagonal(tab,2)):
            if eh_posicao_livre(tab,3):
                bifurcacoes += (3,)     
    # possiveis intersecoes na posicao 4
    if (jog in obter_linha(tab,2) and jog in obter_coluna(tab,1)):
        if (-jog not in obter_linha(tab,2) and -jog not in obter_coluna(tab,1)):
            if eh_posicao_livre(tab,4):
                bifurcacoes += (4,)     
    # possiveis intersecoes na posicao 5
    if (jog in obter_linha(tab,2) and jog in obter_coluna(tab,2)):
        if (-jog not in obter_linha(tab,2) and -jog not in obter_coluna(tab,2)):
            if eh_posicao_livre(tab,5):
                bifurcacoes += (5,)            
    elif (jog in obter_linha(tab,2)) and (jog in obter_diagonal(tab,1)):
        if (-jog not in obter_linha(tab,2) and -jog not in obter_diagonal(tab,1)):
            if eh_posicao_livre(tab,5):
                bifurcacoes += (5,)                
    elif (jog in obter_linha(tab,2)) and (jog in obter_diagonal(tab,2)):
        if (-jog not in obter_linha(tab,2) and -jog not in obter_diagonal(tab,2)):
            if eh_posicao_livre(tab,5):
                bifurcacoes += (5,)                
    elif (jog in obter_coluna(tab,2)) and (jog in obter_diagonal(tab,2)):
        if (-jog not in obter_coluna(tab,2) and -jog not in obter_diagonal(tab,2)):
            if eh_posicao_livre(tab,5):
                bifurcacoes += (5,)                
    elif (jog in obter_coluna(tab,2)) and (jog in obter_diagonal(tab,1)):
        if (-jog not in obter_coluna(tab,2) and -jog not in obter_diagonal(tab,1)):
            if eh_posicao_livre(tab,5):
                bifurcacoes += (5,)                
    elif (jog in obter_diagonal(tab,1)) and (jog in obter_diagonal(tab,2)):
        if (-jog not in obter_diagonal(tab,1) and -jog not in obter_diagonal(tab,2)):
            if eh_posicao_livre(tab,5):
                bifurcacoes += (5,)
    # possiveis intersecoes na posicao 6
    if (jog in obter_linha(tab,2) and jog in obter_coluna(tab,3)):
        if (-jog not in obter_linha(tab,2) and -jog not in obter_coluna(tab,3)):
            if eh_posicao_livre(tab,6):
                bifurcacoes += (6,) 
    # possiveis intersecoes na posicao 7
    if (jog in obter_linha(tab,3) and jog in obter_coluna(tab,1)):
        if (-jog not in obter_linha(tab,3) and -jog not in obter_coluna(tab,1)):
            if eh_posicao_livre(tab,7):
                bifurcacoes += (7,)                
    elif (jog in obter_linha(tab,3)) and (jog in obter_diagonal(tab,2)):
        if (-jog not in obter_linha(tab,3) and -jog not in obter_diagonal(tab,2)):
            if eh_posicao_livre(tab,7):
                bifurcacoes += (7,)              
    elif (jog in obter_coluna(tab,1) and jog in obter_diagonal(tab,2)):
        if (-jog not in obter_coluna(tab,1) and -jog not in obter_diagonal(tab,2)):
            if eh_posicao_livre(tab,7):
                bifurcacoes += (7,)     
    # possiveis intersecoes na posicao 8
    if (jog in obter_linha(tab,3) and jog in obter_coluna(tab,2)):
        if (-jog not in obter_linha(tab,3) and -jog not in obter_coluna(tab,2)):
            if eh_posicao_livre(tab,8):
                bifurcacoes += (8,)     
    # possiveis intersecoes na posicao 9
    if (jog in obter_linha(tab,3) and jog in obter_coluna(tab,3)):
        if (-jog not in obter_linha(tab,3) and -jog not in obter_coluna(tab,3)):
            if eh_posicao_livre(tab,9):
                bifurcacoes += (9,)              
    elif (jog in obter_linha(tab,3)) and (jog in obter_diagonal(tab,1)):
        if (-jog not in obter_linha(tab,3) and -jog not in obter_diagonal(tab,1)):
            if eh_posicao_livre(tab,9):
                bifurcacoes += (9,)                
    elif (jog in obter_coluna(tab,3) and jog in obter_diagonal(tab,1)):
        if (-jog not in obter_coluna(tab,3) and -jog not in obter_diagonal(tab,1)):
            if eh_posicao_livre(tab,9):
                bifurcacoes += (9,)                
    return bifurcacoes
# tabuleiro x jog -> posicao
def atc_bifurcacao(tab,jog):
    ''' nesta funcao ele ve quais sao as posicoes onde e possivel para o jogador
    fazer bifurcacao, e caso existam devolve a primeira posicao onde isso seja
    possivel '''
    possiveis = obter_bifurcacoes(tab,jog)
    if possiveis != ():
        p = possiveis[0]
        marcar_posicao(tab,jog,p)
        return p
    else:
        return False
    
# tabuleiro x jogador -> posicao    
def def_bifurcacao(tab,jog):
    '''recebe um tabuleiro e um inteiro que corresponde ao jogador, a funcao 
    comeca por ver as possibilidades de bifurcacao para o adversario, caso nao
    haja possibilidades de bifurcacao ele passa para o outro criterio da 
    estrategia perfeito, caso haja uma possibilidade , a funcao devolve essa 
    posicao , e caso haja mais que uma , a funcao vai devolver a primeira posicao
    livre em que seja possivel fazermos uma dupla e que nao esteja nas
    possibilidades de bifurcacao do adversario'''
    possiveis = obter_bifurcacoes(tab,-jog)
    if possiveis == ():
        return False
    if len(possiveis) == 1:
        p = possiveis[0]
        return p 
    else:
        p = 1
        while (p in possiveis or (not eh_posicao_livre(tab,p)) ):
            p = p +1
        marcar_posicao(tab,jog,p)
        return p

# cadeia de carateres x cadeia de careteres -> cadeia de carateres    
def jogo_do_galo(letra,est):
    '''esta e a funcao principal de jogo, recebe uma cadeia de carateres que pode
    ser X ou O , e corresponde ao simbolo que o  jogador ira jogar , e recebe 
    outra cadeia de carateres que corresponde a estrategia do computador, esta
    funcao e um while ate que alguem ganhe ou acabem as posicoes livres e seja 
    um empate'''
    tab = ((0,0,0),(0,0,0),(0,0,0))
    if not(letra=='X' or letra=='O' or est=='basico' or est=='normal' or est=='perfeito'):
        raise ValueError('jogo do galo: algum dos argumentos e invalido')
    else:
        print('Bem-vindo ao JOGO DO GALO.')
        print('O jogador joga com '"'"+letra+"'.")
        if letra =='X':
            jog = 1
        if letra =='O':
            jog = -1
        livres = obter_posicoes_livres(tab)
        if letra == 'X':
            while jogador_ganhador(tab) == 0 and (livres != ()):
                pos = escolher_posicao_manual(tab)
                tab = marcar_posicao(tab,jog,pos)
                print(tabuleiro_str(tab))
                livres = obter_posicoes_livres(tab)
                if jogador_ganhador(tab) == 0 and (livres!= ()):
                    print('Turno do computador ('+est+'):')
                    pos = escolher_posicao_auto(tab,-jog,est)
                    tab = marcar_posicao(tab,-jog,pos)
                    print(tabuleiro_str(tab))
                livres = obter_posicoes_livres(tab)
        if letra == 'O':
            while jogador_ganhador(tab) == 0 and (livres != ()):
                print('Turno do computador ('+est+'):')
                pos = escolher_posicao_auto(tab,-jog,est)
                tab = marcar_posicao(tab,-jog,pos)
                print(tabuleiro_str(tab))
                livres = obter_posicoes_livres(tab)  
                if jogador_ganhador(tab) == 0 and (livres!= ()):
                    pos = escolher_posicao_manual(tab)
                    tab = marcar_posicao(tab,jog,pos)
                    print(tabuleiro_str(tab))
                livres = obter_posicoes_livres(tab)        
        if jogador_ganhador(tab)==0:
            return 'EMPATE'
        if jogador_ganhador(tab)== 1:
            return 'X'
        if jogador_ganhador(tab)==-1:
            return 'O'
