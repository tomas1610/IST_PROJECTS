# Tomas Duarte Fonseca Marques ist199340
# str x str -> posicao
def cria_posicao(c,l):
    '''recebe 2 string como argumento , uma corresponde a coluna e a outra corresponde
    a linha , esta funcao da return da representacao que escolhi para a posicao
    que e uma lista (coluna,linha), caso os argumentos sejam invalidos apresenta uma 
    mensagem de erro'''
    colunas = ['a','b','c']
    linhas = ['1','2','3']    
    if not(type(c) == str and type(l)==str and c in colunas and l in linhas):
        raise ValueError('cria_posicao: argumentos invalidos')
    else:
        return [c,l]
# posicao -> posicao
def cria_copia_posicao(pos):
    '''recebe uma posicao , caso seja uma posicao , gera uma copia da mesma , 
    caso contrario apresenta uma mensagem de erro'''
    if eh_posicao(pos):
        return [pos[0]] + [pos[1]]
    else:
        raise ValueError('cria_copia_posicao: argumento invalido')

# posicao -> str
def obter_pos_c(pos):
    '''recebe uma posicao de acordo com a minha representacao e retorna a string
    correspondente a coluna '''
    return pos[0]

# posicao -> str
def obter_pos_l(pos):
    '''recebe uma posicao de acordo com a minha representacao e retorna a string
    correspondente a linha'''
    return pos[1]

# universal -> booleano
def eh_posicao(pos):
    '''recebe um argumento , e caso seja uma posicao de acordo com a minha representacao
    retorna True , senao retorna False'''
    colunas = ['a','b','c']
    linhas = ['1','2','3']
    if type(pos)== list and obter_pos_c(pos) in colunas\
       and obter_pos_l(pos)in linhas:
        return True
    else:
        return False

# posicao x posicao -> booleano    
def posicoes_iguais(pos1,pos2):
    '''recebe duas posicoes e retorna True , apenas se forem posicoes , e se forem 
    iguais , caso isto nao se verifique retorna False'''
    if pos1 == pos2 and eh_posicao(pos1) and eh_posicao(pos2):
        return True
    else:
        return False

# posicao -> string    
def posicao_para_str(pos):
    '''recebe uma posicao e retorna a string correspondente a mesma'''
    return pos[0] + pos[1] 
# posicao -> tuplo de posicoes    
def obter_posicoes_adjacentes(pos):
    '''recebe uma posicao , e retorna um tuplo com todas as posicoes que lhe sao 
    adjacentes'''
    posicoes=[cria_posicao('a','1'),cria_posicao('b','1'),cria_posicao('c','1')]
    posicoes = posicoes + [cria_posicao('a','2'),cria_posicao('c','2')]
    posicoes = posicoes + [cria_posicao('a','3'),cria_posicao('b','3')]
    posicoes = posicoes + [cria_posicao('c','3')]
    if posicoes_iguais(pos,cria_posicao('a','1')):
        return tuple([cria_posicao('b','1'),cria_posicao('a','2'),\
                cria_posicao('b','2')])
    if posicoes_iguais(pos,cria_posicao('a','2')):
        return tuple([cria_posicao('a','1'),cria_posicao('b','2'),\
                cria_posicao('a','3')])
    if posicoes_iguais(pos,cria_posicao('a','3')):
        return tuple([cria_posicao('a','2'),cria_posicao('b','2'),\
                cria_posicao('b','3')])
    if posicoes_iguais(pos,cria_posicao('b','1')):
        return tuple([cria_posicao('a','1'),cria_posicao('c','1'),\
                cria_posicao('b','2')])
    if posicoes_iguais(pos,cria_posicao('b','3')):
        return tuple([cria_posicao('b','2'),cria_posicao('a','3'),\
                cria_posicao('c','3')])
    if posicoes_iguais(pos,cria_posicao('c','1')):
        return tuple([cria_posicao('b','1'),cria_posicao('b','2'),\
                cria_posicao('c','2')])
    if posicoes_iguais(pos,cria_posicao('c','2')):
        return tuple([cria_posicao('c','1'),cria_posicao('b','2'),\
                cria_posicao('c','3')])
    if posicoes_iguais(pos,cria_posicao('c','3')):
        return tuple([cria_posicao('b','2'),cria_posicao('c','2'),\
                cria_posicao('b','3')])
    if posicoes_iguais(pos,cria_posicao('b','2')):
        return tuple(posicoes)

# str -> peca
def cria_peca(peca):
    '''recebe uma string corresponde ao X, O ou nada , e retorna a peca correspondente
    aos mesmo de acordo com a representacao que escolhi , caso a string introduzida 
    nao corresponda a nenhuma das validas , o programa apresenta uma mensagem de erro'''
    pecas = { 'X' : [1],
              'O' : [-1],
              ' ' : [0] }
    if not (type(peca) == str and peca in pecas):
        raise ValueError('cria_peca: argumento invalido')
    return pecas[peca]

# peca -> peca    
def cria_copia_peca(peca):
    '''recebe uma peca , e caso seja uma peca valida , retorna uma copia da mesma
    senao apresenta uma mensagem de erro'''
    if eh_peca(peca):
        return [peca[0]]
    raise ValueError('cria_copia_peca: argumento invalido')

# universal -> booleano
def eh_peca(peca):
    '''recebe um argumento , e caso corresponda a uma peca de acordo com a minha
    representacao , retorna True , caso contrario retorna False'''
    pecas = ([1],[-1],[0])
    if peca == () or peca == []:
        return False
    if ( type(peca) == list and type(peca[0]) == int and peca in pecas):
        return True
    else:
        return False

# peca x peca -> booleano    
def pecas_iguais(peca1,peca2):
    '''recebe duas posicoes ,e retorna True caso sejam ambos os argumentos correspondam
    a pecas , e sejam iguais, senao retorna False'''
    if eh_peca(peca1) and eh_peca(peca2) and peca1 == peca2:
        return True
    else:
        return False

# peca -> str
def peca_para_str(peca):
    '''recebe uma peca e retorna a string correpondente a mesma'''
    if peca == [1]:
        return '[X]'
    if peca == [-1]:
        return '[O]'
    if peca == [0]:
        return '[ ]'
# peca -> N    
def peca_para_inteiro(peca):
    '''recebe uma peca , e retorna o inteiro correpondente da mesma'''
    if pecas_iguais(peca,cria_peca('X')):
        return 1
    if pecas_iguais(peca,cria_peca('O')):
        return -1        
    if pecas_iguais(peca,cria_peca(' ')):
        return 0

# {} -> tabuleiro    
def cria_tabuleiro():
    ''' nao recebe nada como argumento , e apenas retorna um tabuleiro vazio'''
    linha = [cria_peca(' '),cria_peca(' '),cria_peca(' ')]
    tabuleiro =[linha,linha,linha]
    return tabuleiro

# tabuleiro -> tabuleiro
def cria_copia_tabuleiro(tab):
    '''recebe um tabuleiro , e caso seja um tabuleiro valido , retorna uma copia
    do mesmo'''
    if eh_tabuleiro(tab):
        return [tab[0],tab[1],tab[2]]
    raise ValueError('cria_copia_tabuleiro: argumento invalido')

# tabuleiro x posicao -> peca
def obter_peca(t,p):
    '''recebe um tabueleiro e uma posicao , de acordo com a minha representacao ,
    e devolve a peca que esta nessa posicao do tabuleiro'''
    col = list(obter_coluna(t,obter_pos_c(p)))
    lin = int(obter_pos_l(p))
    return col[lin - 1]

# tabuleiro x str -> tuplo de pecas
def obter_vetor(tab, string):
    '''recebe um tabuleiro e uma string correspondente a uma coluna , ou linha,
    e devolve a linha ou coluna correspondente'''
    colunas = ('a','b','c')
    if string in colunas:
        return obter_coluna(tab,string)
    else:
        return obter_linha(tab,string)

# tabuleiro x str -> tuplo de pecas        
def obter_coluna(tab,col):
    ''' e uma funcao auxiliar , que recebe um tabuleiro valido e uma string correspondente
    a uma coluna , e retorna um tuplo das pecas que estao nessa coluna'''
    if col == 'a':
        return (tab[0][0],) + (tab[1][0],) + (tab[2][0],)
    if col == 'b':
        return (tab[0][1],) + (tab[1][1],) + (tab[2][1],)
    if col == 'c':
        return (tab[0][2],) + (tab[1][2],) + (tab[2][2],)     
# tabuleiro x str -> tuplo de pecas    
def obter_linha(tab,lin):
    ''' e uma funcao auxiliar , que recebe um tabuleiro valido e uma string correspondente
    a uma linha , e retorna um tuplo das pecas que estao nessa linha'''    
    linha = int(lin)
    return tuple(tab[linha - 1])

# tabuleiro x peca x posicao -> tabuleiro
def coloca_peca(tab,peca,pos):
    '''recebe um tabuleiro, uma peca e uma posicao , tudo de acordo com a representacao
    escolhida anteriormente , e retorna um tabuleiro correpondente a colocacao da
    peca escolhida na posicao escolhida do tabuleiro'''
    coluna = obter_pos_c(pos)
    if coluna == 'a':
        col = 0
    if coluna == 'b':
        col = 1
    if coluna == 'c':
        col = 2
    lin = int(obter_pos_l(pos))
    col_lin = (coluna,lin)
    linha = list(obter_linha(tab,lin))
    linha[col] = peca
    tab[lin - 1] = linha
    
    return tab

# tabuleiro x posicao -> tabuleiro
def remove_peca(tab,pos):
    '''recebe um tabuleiro e uma posicao , e apenas retorna o tabuleiro correspondente
    da remocao da peca correspondente a posicao passada como argumento'''
    return coloca_peca(tab,cria_peca(' '),pos)

# tabuleiro x posicao x posicao -> tabuleiro
def move_peca(tab,p1,p2):
    '''recebe um tabuleiro , e duas posicoes , e retorna o tabuleiro correspondente
    ao movimento da peca que ocupa a primeira posicao , para a segunda posicao'''
    pec = obter_peca(tab,p1)
    coloca_peca(tab,pec,p2)
    remove_peca(tab,p1)
    return tab 

# linha -> booleano    
def eh_linha(lin):
    '''recebe uma linha e retorna True , caso seja uma linha , e False caso nao seja'''
    if not (type(lin) == list and len(lin) == 3):
        return False
    for e in lin:
        if not eh_peca(e):
            return False
        else:
            return True

# universal -> booleano        
def eh_tabuleiro(tab):
    '''recebe um tabuleiro e retorna True , caso seja um tabuleiro , de acordo com 
    as restricoes definidas no enunciado, caso contrario retorna False'''
    if type(tab) != list or len(tab) != 3:
        return False
    for i in tab:
        if type(i) != list or len(i) != 3:
            return False
        for e in i:
            if not eh_peca(e):
                return False

    pecas1 = len(obter_posicoes_jogador(tab,cria_peca('X')))
    pecas2 = len(obter_posicoes_jogador(tab,cria_peca('O')))
    if pecas1 > 3 or pecas2 > 3:
        return False
    
    dif = pecas1 - pecas2
    if (dif < -1 or dif > 1):
        return False
    if dois_ganhadores(tab):
        return False
    return True

# tabuleiro x posicao -> booleano
def eh_posicao_livre(tab,pos):
    '''recebe um tabuleiro e uma posicao , caso essa posicao esteja livre retorna 
    True , senao retorna False'''
    if obter_peca(tab,pos) == cria_peca(' '):
        return True
    return False

# tabuleiro x tabuleiro -> booleano    
def tabuleiros_iguais(t1,t2):
    '''recebe dois tabuleiros , e caso sejam ambos tabuleiros , e sejam iguais , 
    retorna True , caso contrario retorna False'''
    if eh_tabuleiro(t1) and eh_tabuleiro(t2) and t1 == t2:
        return True
    return False

# tabuleiro -> str
def tabuleiro_para_str(tab):
    '''recebe um tabuleiro , e converte-o para string , desenhando assim o tabuleiro
    e adicionando as letras que correspondem as colunas e as linhas '''
    rep_t = '   a   b   c\n'
    rep_t = rep_t + \
        '1 ' + escreve_linha(tab[0]) + '\n'+\
        '   | \ | / |\n' + \
        '2 ' + escreve_linha(tab[1])+'\n' + \
        '   | / | \ |\n' +\
        '3 ' + escreve_linha(tab[2])
    return rep_t

# linha -> str    
def escreve_linha(l):
    '''funcao auxiliar da tabuleiro_para_str recebe uma linha , e converte-a para string,'''
    linha = ''
    for e in l:
        if e == cria_peca('X'):
            (e) = ('[X]')
        if e == cria_peca('O'):
            (e) = ('[O]')
        if e == cria_peca(' '):
            (e) = ('[ ]')
        linha = linha + str(e) + '- ' 
        linha = linha[:-1]
    return (linha[:-1]) 

# tuplo -> tabuleiro
def tuplo_para_tabuleiro(tup):
    '''devolve o tabuleiro que e representado pelo tuplo t
     com 3 tuplos, cada um deles contendo 3 valores inteiros iguais a 1, -1 ou 0,
     tal como no primeiro projeto'''
    tab = list(tup)
    for i in range(0,3):
        lin = tab[i]
        m = list(lin)
        tab[i] = m
        for i in range(0,3):
            e = lin[i]
            e = [e]
            m[i] = e
    return tab  

# tabuleiro -> booleano
def dois_ganhadores(tab):
    ''' funcao auxiliar , que recebe um tabuleiro e devolve True , caso haja mais do
    que um ganhador , e False , caso haja apenas 1 ou 0 ganhadores'''
    ganhadores = 0
    for i,c in (('1','a'),('2','b'),('3','c')):
        if vetor_igual(obter_vetor(tab,i),(cria_peca('X'),cria_peca('X'),cria_peca('X'))) \
           or vetor_igual(obter_vetor(tab,c),(cria_peca('X'),cria_peca('X'),cria_peca('X'))):          
            ganhadores += 1
        elif vetor_igual(obter_vetor(tab,i),(cria_peca('O'),cria_peca('O'),cria_peca('O')))\
             or vetor_igual(obter_vetor(tab,c),(cria_peca('O'),cria_peca('O'),cria_peca('O'))):
            ganhadores += 1
    if ganhadores > 1:
        return True
    return False
    
    
# tabuleiro -> peca
def obter_ganhador(tab): 
    '''recebe um tabuleiro , e devolve a peca do jogador vencedor , caso haja algum,
    caso nao haja, retorna a peca 0 '''
    for i,c in (('1','a'),('2','b'),('3','c')):
        if vetor_igual(obter_vetor(tab,i),(cria_peca('X'),cria_peca('X'),cria_peca('X'))) \
           or vetor_igual(obter_vetor(tab,c),(cria_peca('X'),cria_peca('X'),cria_peca('X'))):          
            return cria_peca('X')
        elif vetor_igual(obter_vetor(tab,i),(cria_peca('O'),cria_peca('O'),cria_peca('O')))\
             or vetor_igual(obter_vetor(tab,c),(cria_peca('O'),cria_peca('O'),cria_peca('O'))):
            return cria_peca('O') 
    return cria_peca(' ')

# vetor x vetor -> booleano        
def vetor_igual(v1,v2):
    ''' funcao auxiliar , que recebe dois vetores , e devolve True caso sejam ambos
    vetores , e sejam iguais , e False caso contrario'''
    if not eh_vetor(v1) and eh_vetor(v2):
        return False
    for i in range(0,3):
        if not pecas_iguais(v1[i],v2[i]):
            return False
    return True

# universal -> booleano
def eh_vetor(vet):
    ''' devolve True , caso o argumento seja um vetor , isto e , se for um tuplo
    com 3 elementos'''
    if not (type(vet) == tuple and len(vet) == 3):
        return False
    for e in vet:
        if not eh_peca(e):
            return False
    return True        

# tabuleiro -> tuplo de posicoes
def obter_posicoes_livres(tab):
    '''recebe um tabuleiro , e devolve um tuplo com todas as posicoes livres existentes'''
    posicoes=[cria_posicao('a','1'),cria_posicao('b','1'),cria_posicao('c','1')]
    posicoes = posicoes + [cria_posicao('a','2'),cria_posicao('b','2')]
    posicoes = posicoes + [cria_posicao('c','2'),cria_posicao('a','3')]
    posicoes = posicoes + [cria_posicao('b','3'),cria_posicao('c','3')]    
    livres = []
    for pos in posicoes:
        if eh_posicao_livre(tab,pos):
            livres = livres + [pos]
    return tuple(livres)
# tabuleiro x peca -> tuplo de posicoes
def obter_posicoes_jogador(tab,peca):
    '''recebe um tabuleiro e uma peca ,e vai devolver um tuplo com todas as posicoes
    ocupadas por essa peca '''
    posicoes=[cria_posicao('a','1'),cria_posicao('b','1'),cria_posicao('c','1')]
    posicoes = posicoes + [cria_posicao('a','2'),cria_posicao('b','2')]
    posicoes = posicoes + [cria_posicao('c','2'),cria_posicao('a','3')]
    posicoes = posicoes + [cria_posicao('b','3'),cria_posicao('c','3')]
    jog = []
    for pos in posicoes:
        if pecas_iguais(obter_peca(tab,pos),peca):
            jog = jog + [pos]
    return tuple(jog)

# tab x peca -> booleano
def esta_presa(tab,peca):
    ''' e uma funcao auxiliar , que recebe um tabuleiro , e uma peca , e tem como
    objetivo devolver True , caso a peca escolhida esteja presa , e False caso contrario'''
    livres = ()
    for pos in obter_posicoes_jogador(tab,peca):
        for a in obter_posicoes_adjacentes(pos):
            if eh_posicao_livre(tab,a):
                livres = livres + (a,)
    if livres != ():
        return False
    return True

# tabuleiro x peca -> tuplo de posicoes        
def obter_movimento_manual(tab,peca):
    '''recebe um tabuleiro e uma peca (representa o jogador a jogar), a funcao ira pedir 
    ao jogador para ecolher uma posicao , ou um movimento , dependendo do tabuleiro, 
    e vai devolver um tuplo com a posicao ou posicoes , dependendo da situacao'''
    if len(obter_posicoes_jogador(tab,peca)) < 3:
        pos = input('Turno do jogador. Escolha uma posicao: ')
        col = ('a1','b1','c1','a2','b2','c2','a3','b3','c3')
        if (pos not in col):
            raise ValueError('obter_movimento_manual: escolha invalida')           
        posicao = [pos[0],pos[1]]
        if not (eh_posicao_livre(tab,posicao) and eh_posicao(posicao)):
            raise ValueError('obter_movimento_manual: escolha invalida')
        return (posicao,)
    if len(obter_posicoes_jogador(tab,peca)) >= 3:
        if not esta_presa(tab,peca):
            pos = input('Turno do jogador. Escolha um movimento: ')
            if len(pos) != 4:
                raise ValueError('obter_movimento_manual: escolha invalida')                
            pos1 = [pos[0],pos[1]]
            pos2 = [pos[2],pos[3]]
            if not (eh_posicao(pos1) and pos1 in obter_posicoes_jogador(tab,peca) \
                    and eh_posicao(pos2) and eh_posicao_livre(tab,pos2) and \
                    pos2 in obter_posicoes_adjacentes(pos1)):
                raise ValueError('obter_movimento_manual: escolha invalida')
            return (pos1,pos2)
        pos = input('Turno do jogador. Escolha um movimento: ')
        pos1 = [pos[0],pos[1]]
        pos2 = [pos[2],pos[3]]
        if posicoes_iguais(pos1,pos2):
            return (pos1,pos2)
        raise ValueError('obter_movimento_manual: escolha invalida')
        
    
# tabuleiro x peca -> tuplo de posicao    
def vitoria_bloqueio(tab,peca):
    '''funcao auxiliar , que recebe um tabuleiro e uma peca e,caso haja alguma
    possibilidade de vitoria , devolve um tuplo com a posicao onde seria possivel
    a vitoria, caso nao haja possibilidade de vitoria , nao devolve nada'''
    for pos in obter_posicoes_livres(tab):
        tab2 = coloca_peca(tab,peca,pos)
        if pecas_iguais(obter_ganhador(tab2),peca):
            tab = remove_peca(tab,pos)
            return (pos,)
        else:
            tab = remove_peca(tab,pos)

# tabuleiro -> tuplo de posicao        
def canto_livre(tab):
    '''funcao auxiliar , recebe um tabuleiro e devolve um tuplo com o primeiro canto
    que encontrar livre , caso nao encontre nenhum , entao nao devolve nada'''
    for pos in (cria_posicao('a','1'),cria_posicao('c','1'),cria_posicao('a','3'),cria_posicao('c','3')):
        if eh_posicao_livre(tab,pos):
            return (pos,)
        
# tabuleiro -> tuplo de posicao        
def lateral_livre(tab):
    '''funcao auxiliar que recebe um tabuleiro e devolve um tuplo com a primeira lateral
    que encontrar livre , caso nao encontre nenhuma , nao devolve nada'''
    for pos in (cria_posicao('b','1'),cria_posicao('a','2'),cria_posicao('c','2'),cria_posicao('b','3')):
        if eh_posicao_livre(pos):
            return (pos,)

# tabuleiro x peca x str -> tuplo de posicoes        
def obter_movimento_auto(tab,peca,strat):
    ''' funcao para o movimento do computador, dependendo do tabuleiro , entrara
    em duas fases diferentes, fase de colocacao ou movimentacao , no fim ira devolver
    um tuplo com a posicao para onde se colocou a peca , ou um tuplo com as posicoes 
    que foram utilizadas no movimento'''
    if len(obter_posicoes_jogador(tab,peca)) < 3:
        return colocacao(tab,peca)
    else:
        return movimento(tab,peca,strat)
        
    
# tabuleiro x peca -> tuplo de posicao    
def colocacao(tab,peca):
    '''funcao auxiliar da obter_movimento_auto , caso esteja na fase de colocacao,
    apenas ira chamar as funcao auxiliares de cada criterio que devolverao , um tuplo 
    com a posicao onde devemos jogar'''
    if vitoria_bloqueio(tab,peca):       
        return vitoria_bloqueio(tab,peca)
    if vitoria_bloqueio(tab,adversario(peca)):
        return vitoria_bloqueio(tab,adversario(peca))
    if eh_posicao_livre(tab,cria_posicao('b','2')):
        return (cria_posicao('b','2'),)
    if canto_livre(tab):
        return canto_livre(tab)
    if lateral_livre(tab):
        return lateral_livre(tab)
 
# tabuleiro x peca x str -> tuplo de posicoes   
def movimento(tab,peca,strat):
    '''funcao auxiliar , utilizada para o metodo de movimento , dependendo da estrategia
    ira devolver funcoes diferentes, devolvendo no fim um tuplo de posicoes , para
    se executar o movimento'''
    for pos in obter_posicoes_jogador(tab,peca):
        if len(obter_posicoes_adjacentes(pos)) != 0:
            if strat == 'facil':
                return facil(tab,peca)
            if strat == 'normal':
                return normal(tab,peca)
            if strat == 'dificil':
                return dificil(tab,peca)
    pos = obter_posicoes_jogador(tab,peca)[0] 
    return (pos,pos)

# tabuleiro x peca -> tuplo de posicoes    
def facil(tab,peca):
    ''' funcao auxiliar , representa a estrategia facil, devolve um tuplo , com uma 
    posicao do jogador , e a primeira posicao adjacente livre que encontrar'''
    for p in obter_posicoes_jogador(tab,peca):
        for a in obter_posicoes_adjacentes(p):
            if eh_posicao_livre(tab,a):
                return (p,a)
 
# tabuleiro x peca -> tuplo de posicoes            
def normal(tab,peca):
    '''funcao auxiliar, representa a estrategia normal ,chama a funcao minimax com
    profundidade maxima de 1 , e devolve um tuplo, que descreve o movimento que o
    algoritmo nos aconselha a fazer'''
    m = minimax(tab,peca,1,[])
    mov = m[1]
    return (mov[0],mov[1])

# tabuleiro x peca -> tuplo de posicoes
def dificil(tab,peca):
    '''funcao auxiliar, representa a estrategia dificil, chama a funcao minimax com
    pronfundidade maxima de 5 , e devolve um tuplo que com o primeiro movimento que
    deve fazer '''
    m = minimax(tab,peca,5,[])
    mov = m[1]
    return (mov[0],mov[1])

# peca -> peca            
def adversario(peca):
    '''funcao auxiliar , com o objetivo de receber uma peca , e devolver a peca
    do adversario'''
    if pecas_iguais(cria_peca('X'),peca):
        return cria_peca('O')
    if pecas_iguais(cria_peca('O'),peca):
        return cria_peca('X') 
    return cria_peca(' ')

#     
def minimax(tab,peca,prof, seq_mov):
    if obter_ganhador(tab) != cria_peca(' ') or prof == 0:
        return peca_para_inteiro(obter_ganhador(tab)), seq_mov
    best_res = -peca_para_inteiro(peca)
    best_seq_mov = []
    for pos in obter_posicoes_jogador(tab,peca):
        for adj in obter_posicoes_adjacentes(pos):
            if eh_posicao_livre(tab,adj):
                tab2 = cria_copia_tabuleiro(tab)
                tab2 = move_peca(tab2,pos,adj)
                new_res,new_seq_mov = minimax(tab2,adversario(peca),prof - 1,seq_mov + [pos,adj])
                if (best_seq_mov == []) or ( pecas_iguais(peca,cria_peca('X')) and (new_res > best_res)) or (pecas_iguais(peca,cria_peca('O')) and ( new_res < best_res)):
                    best_res = new_res
                    best_seq_mov = new_seq_mov
    return best_res, best_seq_mov

# str -> booleano
def eh_strat(strat):
    '''recebe uma string que representa a estrategia ,e devolve True , caso seja uma
    das estrategias definidas , e False caso contrario'''
    est = ('facil','normal','dificil')
    if strat in est:
        return True
    return False

# str -> booleano
def eh_peca_str(pec_str):
    ''' recebe uma string e retorna True , caso seja o resultado da conversaro de 
    peca_para_string de alguma peca, e devolve False caso contrario'''
    pecas = ('[X]','[O]','[ ]')
    if pec_str in pecas:
        return True
    return False

# str x str -> str
def moinho(pec_str,strat):
    ''' funcao principal de jogo , recebe uma string , representando a peca , com 
    que o jogador ira jogar , e outra string , que representa a estrategia do computador
    , a funcao ira continuar ,a pedir posicoes e a marcar e mover posicoes , ate 
    haja um ganhador e o jogo acaba, quando alguem tiver ganho , o jogo termina
    devolvendo a peca do ganhador'''
    if not ( eh_peca_str(pec_str) and eh_strat(strat)):
        raise ValueError('moinho: argumentos invalidos')
    print('Bem-vindo ao JOGO DO MOINHO. Nivel de dificuldade',strat+'.')
    tab = cria_tabuleiro()
    print(tabuleiro_para_str(tab))
    if pec_str == '[X]':
        peca = cria_peca('X')
        adv = adversario(peca)
        while obter_ganhador(tab) == cria_peca(' '):
            pos = obter_movimento_manual(tab,peca)
            if len(pos) == 1:
                tab = coloca_peca(tab,peca,pos[0])
            else:
                tab = move_peca(tab,pos[0],pos[1])
            print(tabuleiro_para_str(tab))
            if obter_ganhador(tab) == cria_peca(' '):
                print('Turno do computador ('+strat+'):')
                pos = obter_movimento_auto(tab,adv,strat)
                if len(pos) == 1:
                    tab = coloca_peca(tab,adv,pos[0])
                else:
                    tab = move_peca(tab,pos[0],pos[1])
                print(tabuleiro_para_str(tab))
                
    if pec_str == '[O]':
        peca = cria_peca('O')
        adv = adversario(peca)
        while obter_ganhador(tab) == cria_peca(' '):
            print('Turno do computador ('+strat+'):')
            pos = obter_movimento_auto(tab,adv,strat)
            if len(pos) == 1:
                tab = coloca_peca(tab,adv,pos[0])
            else:
                tab = move_peca(tab,pos[0],pos[1])
            print(tabuleiro_para_str(tab))
            if obter_ganhador(tab) == cria_peca(' '):
                pos = obter_movimento_manual(tab,peca)
                if len(pos) == 1:
                    tab = coloca_peca(tab,peca,pos[0])
                else:
                    if not esta_presa(tab,peca):
                        tab = move_peca(tab,pos[0],pos[1])
                print(tabuleiro_para_str(tab))                
    return peca_para_str(obter_ganhador(tab))