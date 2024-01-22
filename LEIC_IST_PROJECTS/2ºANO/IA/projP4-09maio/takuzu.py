# takuzu.py: Template para implementação do projeto de Inteligência Artificial 2021/2022.
# Devem alterar as classes e funções neste ficheiro de acordo com as instruções do enunciado.
# Além das funções e classes já definidas, podem acrescentar outras que considerem pertinentes.

# Grupo 90:
# 99340 Tomás Marques
# 99330 Stanislaw Talejko

import sys
from search import (
    Problem,
    Node,
    astar_search,
    breadth_first_tree_search,  
    depth_first_tree_search,
    greedy_search,
    recursive_best_first_search,
)

from sys import stdin
import numpy as np

class TakuzuState:
    state_id = 0

    def __init__(self, board):
        self.board = board
        self.id = TakuzuState.state_id
        changes = 1
        while (changes != 0):
            changes = self.number_1_0()
            changes += self.check_mandatory()
        self.board.get_empty_positions()
        TakuzuState.state_id += 1



    def change_row(self, row : int, value : int):
        changed = 0
        n = len(self.board.board)
        for i in range(0,n):
            if self.board.board[row][i] == 2:
                self.board.board[row][i] = value
                changed = 1
        return changed

    def change_collun(self, col : int, value : int):
        changed = 0
        n = len(self.board.board)
        for i in range(0,n):
            if self.board.board[i][col] == 2:
                self.board.board[i][col] = value 
                changed = 1
        return changed

    def number_1_0(self):
        changes = 0
        n = len(self.board.board)
        if n % 2 == 0:
            max_value= n /2
            for l in range(0,n):
                uns = np.count_nonzero(self.board.board[l] == 1)
                zeros = np.count_nonzero(self.board.board[l] == 0)
                if uns == max_value:
                    if self.change_row(l,0) == 1:
                        changes += 1
                if zeros == max_value:
                    if self.change_row(l,1) == 1:
                        changes += 1
            transposta = np.transpose(self.board.board)
            for l in range(0,n):
                uns = np.count_nonzero(transposta[l] == 1)
                zeros = np.count_nonzero(transposta[l] == 0)
                if uns == max_value:
                    if self.change_collun(l,0) == 1:
                        changes += 1
                if zeros == max_value:
                    if self.change_collun(l,1) == 1:
                        changes += 1
        else:
            max_value= n /2 + 0.5
            for l in range(0,n):
                uns = np.count_nonzero(self.board.board[l] == 1)
                zeros = np.count_nonzero(self.board.board[l] == 0)
                if uns == max_value:
                    if self.change_row(l,0) == 1:
                        changes += 1
                if zeros == max_value:
                    if self.change_row(l,1) == 1:
                        changes += 1
            transposta = np.transpose(self.board.board)
            for l in range(0,n):
                uns = np.count_nonzero(transposta[l] == 1)
                zeros = np.count_nonzero(transposta[l] == 0)
                if uns == max_value:
                    if self.change_collun(l,0) == 1:
                        changes += 1
                if zeros == max_value:
                    if self.change_collun(l,1) == 1:
                        changes += 1
        return changes

    def check_mandatory(self):
        n = len(self.board.board)
        changes = 0
        for i in range(0,n):
            for j in range(0,n):
                if (self.board.get_number(i,j) == 2):
                    adj = self.get_adjacents(i,j)
                    if adj < 2:
                        self.board.board[i][j] = adj
                        changes += 1
        return changes

    def get_adjacents(self, row : int, col : int):
        adjacents = []
        adjacents.append((self.board.adjacent_horizontal_numbers(row,col)))
        adjacents.append((self.board.adjacent_vertical_numbers(row,col)))
        n = len(self.board.board)
        if row <= 1:
            adjacents.append(((self.board.board[row+1][col],self.board.board[row+2][col])))
        if row > 1 and row < n -2:
            adjacents.append((self.board.board[row-1][col],self.board.board[row-2][col]))
            adjacents.append((self.board.board[row+1][col],self.board.board[row+2][col]))
        if row >= n-2:
            adjacents.append((self.board.board[row-1][col],self.board.board[row-2][col]))
        if col <= 1:
            adjacents.append((self.board.board[row][col+1],self.board.board[row][col+2]))
        if col > 1 and col < n-2:
            adjacents.append((self.board.board[row][col+1],self.board.board[row][col+2]))
            adjacents.append((self.board.board[row][col-1],self.board.board[row][col-2]))
        if col >= n-2:
            adjacents.append((self.board.board[row][col-1],self.board.board[row][col-2]))
        if ((0,0) in adjacents and (1,1) in adjacents):
            return 3
        if ((0,0) in adjacents):
            return 1
        if ((1,1) in adjacents):
            return 0
        return 2      

    def __lt__(self, other):
        return self.id < other.id
	# j
    # TODO: outros metodos da classe


class Board:
    """Representação interna de um tabuleiro de Takuzu."""

    def __init__(self, table):
        self.board = np.array(table)

    def __str__(self):
        ster = ""
        for l in range(0,len(self.board)):
            for c in range(0,len(self.board)):
                    ster += str(self.board[l][c])
                    if c != len(self.board)-1:
                        ster += "\t"

            ster += "\n"
        return ster

    def get_empty_positions(self):   # retorna uma lista com as posicoes que tem dominio >1 , ou seja nao tem nenhuma jogado obrigatoria
        positions = []             # vamos ter de escolher , qual a melhor jogada
        n = len(self.board)
        for i in range(0,n):
            for j in range(0,n):
                if (self.board[i][j] == 2):
                    positions.append([i,j])
        self.positions = positions

    def get_number(self, row: int, col: int) -> int:
        """Devolve o valor na respetiva posição do tabuleiro."""
        return self.board[row][col]

    def adjacent_vertical_numbers(self, row: int, col: int) -> (int, int):
        """Devolve os valores imediatamente abaixo e acima,
        respectivamente."""
        if row == 0:
            return (None,self.board[row+1,col])
        elif row == len(self.board)-1:
            return (self.board[row-1][col],None)
        else:
            return (self.board[row-1][col],self.board[row+1,col])

    def adjacent_horizontal_numbers(self, row: int, col: int) -> (int, int):
        """Devolve os valores imediatamente à esquerda e à direita,
        respectivamente."""
        if col == 0:
            return (None,self.board[row,col+1])
        elif col == len(self.board)-1:
            return (self.board[row][col-1],None)
        else:
            return (self.board[row][col-1],self.board[row,col+1])
        

    @staticmethod
    def parse_instance_from_stdin():
        """Lê o test do standard input (stdin) que é passado como argumento
        e retorna uma instância da classe Board.
        Por exemplo:
            $ python3 takuzu.py < input_T01
            > from sys import stdin
            > stdin.readline()
        """
        n = int(sys.stdin.readline())

        board = []
        for i in range(n):
            row = sys.stdin.readline()
            list_row = row.split('\t')
            list_row = list(map(int,list_row))
            board.append(list_row)
        return Board(board)

    # TODO: outros metodos da classe

def alldif(this):
    n = len(this)
    for i in range(0, n):
        for j in range(i + 1, n):
            if list(this[i]) == list(this[j]):
                #print("verifying", this[i], this[j])
                return False
    return True


class Takuzu(Problem):
    def __init__(self, board: Board):
        """O construtor especifica o estado inicial."""
        self.state = TakuzuState(board)


    def actions(self, state: TakuzuState):
        """Retorna uma lista de ações que podem ser executadas a
        partir do estado passado como argumento."""
        a = []
        n = len(state.board.board)
        for l in range(0,n):
            for c in range(0,n):
                if self.state.board.get_number(l,c) == 2:
                    a += [(l,c,0)]	# verificamos se a ação é legal aqui ou no goal_test? CSP
                    a += [(l,c,1)]
        return a

    def check_line_cols(self,row:int,col:int):
        n = 0
        line = self.state.board.board[row]
        for i in self.state.board.board:
            if (np.array_equiv(i,line)):
                n += 1
        if n >= 2:
            return True
        n = 0
        transposta = np.transpose(self.state.board.board)
        coll = transposta[col]
        for i in transposta:
            if (np.array_equiv(i,coll)):
                n += 1
        if n >= 2:
            return True
        return False

    def is_possible(self, row : int , col : int , value : int):
        n = len(self.state.board.board)
        adj = self.state.get_adjacents(row,col)
        if (value == (adj - 1) * -1 or adj == 3):    
            return False
        transposta = np.transpose(self.state.board.board)
        if (n %2 == 0):
            max_value = n / 2
        else:
            max_value = n /2 + 0.5
        if (value == 1):
            uns = np.count_nonzero(self.state.board.board[row] == 1) + 1
            uns_t = np.count_nonzero(transposta[col] == 1) + 1
            if (uns > max_value or uns_t > max_value):
                return False
        else :
            zeros = np.count_nonzero(self.state.board.board[row] == 0) + 1
            zeros_t = np.count_nonzero(transposta[col] == 0) + 1
            if (zeros > max_value or zeros_t > max_value):
                return False
        self.state.board.board[row][col] = value
        transposta = np.transpose(self.state.board.board)
        if (2 not in self.state.board.board[row] and 2 not in transposta[col]):
            if self.check_line_cols(row,col):
                self.state.board.board[row][col] = 2
                return False
        return True        

    def solve(self):
        n = len(self.state.board.board)
        for row in range(0,n):
            for col in range(0,n):
                if self.state.board.board[row][col] == 2:   # para todas as posicoes livres 
                    for value in range (0,2):
                        if self.is_possible(row,col,value):        # vemos se é possivel colocar esse valor nessa posicao
                            self.state.board.board[row][col] = value
                            self.solve()        # se for possivel alteramos essa posicao , e chamamos de novo o solve 
                            if self.goal_test(self.state) == False:
                                self.state.board.board[row][col] = 2
                    return



    def result(self, state: TakuzuState, action):
        """Retorna o estado resultante de executar a 'action' sobre
        'state' passado como argumento. A ação a executar deve ser uma
        das presentes na lista obtida pela execução de
        self.actions(state)."""
        # TODO
        state1 = TakuzuState(board)
        state1.board.board[action[0]][action[1]] = action[2]
        return state1

    def goal_test(self, state: TakuzuState):
        """Retorna True se e só se o estado passado como argumento é
        um estado objetivo. Deve verificar se todas as posições do tabuleiro
        estão preenchidas com uma sequência de números adjacentes."""
        #print("begin te")
        for l in state.board.board:
            if 2 in l:
                return False
        if alldif(state.board.board):
            if alldif(np.transpose(state.board.board)):
                return True
        return False


    def h(self, node: Node):
        """Função heuristica utilizada para a procura A*."""
        # TODO
        pass

    # TODO: outros metodos da classe


if __name__ == "__main__":
    # TODO:
    # Ler o ficheiro de input de sys.argv[1], 
    # Usar uma técnica de procura para resolver a instância,
    # Retirar a solução a partir do nó resultante, 
    # Imprimir para o standard output no formato indicado.
	
    board = Board.parse_instance_from_stdin()
    problem = Takuzu(board)
    problem.solve()     # chamar funcao para resolver o problema
    print(board,end = '')

    pass