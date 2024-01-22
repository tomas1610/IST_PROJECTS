#include <iostream>
#include <sstream>
#include <stdio.h>
#include <string.h>
#include <initializer_list>
#include <vector>
#include <algorithm>
#include <unordered_map>

using namespace std;

typedef struct{
    vector<int> pi;
    bool visited;
    bool recStack;
} node;

bool cyclic_aux(node graph[], int v){
    if (!graph[v-1].visited){

        graph[v-1].visited = true;
        graph[v-1].recStack = true;

        vector<int> pis = graph[v-1].pi;

        for (int i : pis){
            if (graph[i-1].visited == false && cyclic_aux(graph,i) == true)
                return true;
            else if (graph[i-1].recStack)
                return true;
        }
    }
    graph[v-1].recStack = false;
    return false;
}

bool check_cyclic(node graph[], int nodes){

    for (int i = 0; i < nodes; i++){
        if (cyclic_aux(graph,i))
            return true;
    }
    return false;
}

vector<int> find_ancestors(node graph[], int v, vector<int> &anc){
    if (v != 0){
        vector<int> pis = graph[v-1].pi;
        for (int p: pis){
            if (p != 0){
                if (graph[p-1].pi[0] != 0)
                    anc.push_back(graph[p-1].pi[0]);
                if (graph[p-1].pi[1] != 0)
                    anc.push_back(graph[p-1].pi[1]);
                if (graph[p-1].pi[0] != 0)
                    anc = find_ancestors(graph,p,anc);
            }            
        }
    }

    return anc;
}

bool check_if_closest(node graph[], int v, vector<int> &anc){
    for (int i : anc){
        vector<int> pis = graph[i-1].pi;
        if (count(pis.begin(),pis.end(),v))
            return false;
    }
    return true;
}

int main(){


    std::ios::sync_with_stdio(false);
    int v1,v2,nodes,arcs, dad, son;
    cin >> v1;
    cin >> v2;
    cin >> nodes;
    cin >> arcs;

    int **table = new int*[nodes];
    for(int i = 0; i< nodes; i++){
        table[i] = new int[2];
    }
    
    node *graph = new node[nodes];

    for (int i = 0; i < nodes; i++){
        for (int j = 0; j < 2; j++){
            table[i][j] = 0;
        }
    }

    for (int i = 0; i < arcs; i++){
        cin >> dad;
        cin >> son; 
        if (table[son-1][0] == 0)
            table[son-1][0] = dad;
        else if (table[son-1][1] == 0)
            table[son-1][1] = dad;
        else {
            cout << "0" << endl;
            return 0;
        }
    }

    for (int i = 0; i < nodes; i++){
        graph[i].pi.push_back(table[i][0]);
        graph[i].pi.push_back(table[i][1]);
        graph[i].visited = false;
        graph[i].recStack = false;
    }

    if (check_cyclic(graph, nodes)){
        cout << "0" << endl;
        return 0;
    }

    vector<int> menor, maior;

    vector<int> anc_f1 = find_ancestors(graph,v1,graph[v1-1].pi);
    anc_f1.push_back(v1);
    vector<int> anc_f2 = find_ancestors(graph,v2,graph[v2-1].pi);
    anc_f2.push_back(v2);

    if (anc_f1.size() >= anc_f2.size()){
        menor = anc_f2;
        maior = anc_f1;
    }
    else{
        menor = anc_f1;
        maior = anc_f2;
    }

    vector<int> final;

    for (int i : menor){
        if (count(maior.begin(),maior.end(),i) && i != 0){
            final.push_back(i);
        }
    }

    sort(final.begin(), final.end());
    final.erase(unique(final.begin(),final.end()),final.end());

    vector<int> res;
    for (int i : final){
        if (check_if_closest(graph, i, final)){
            res.push_back(i);
        }
    }


    if (res.size() == 0){
        cout << "-" << endl;
        return 0;
    }

    for (int i : res){
        cout << i;
        cout << " ";
    }
    cout << endl;
    return 0;

}