#include <iostream>
#include <initializer_list>
#include <vector>
#include <sstream>
#include <algorithm>
#include <unordered_map>

using namespace std;

void LCIS(vector<int> v1, int n1, vector<int> v2, int n2){

    vector<int> table(n2,0);
  
    for (int i=0; i<n1; i++){
        int current = 0;
  
        for (int j=0; j<n2; j++){
            if (v1[i] == v2[j]){
                if (current + 1 > table[j])
                    table[j] = current + 1;
            }

            if (v1[i] > v2[j]){
                if (table[j] > current)
                    current = table[j];
            }
        }
    }
  
    int result = 0;
    for (int i=0; i<n2; i++){
        if (table[i] > result)
           result = table[i];
    }
  
    printf("%d\n",result);
}

void LIS(vector<int> seq){
    int n = seq.size();
    if (n == 0)
        printf("0");
 
    vector <int> dp_l(n,1);
    vector <int> dp_c(n,1);

    int cur_l, cur_c;
    int cur_max, cur_mcount;

    cur_max = 1;
    cur_mcount = 0;
 
    for (int i = 0; i < n; i++){
        cur_l = 1;
        cur_c = 1;
        for (int j = 0; j < i; j++){

            if (seq[i] <= seq[j]){
                continue;
            }
 
            if  (dp_l[j] + 1 > cur_l){
                cur_l = dp_l[j] + 1;
                cur_c = dp_c[j];
            }
            else if (dp_l[j] + 1 == cur_l){
                cur_c += dp_c[j];
            }
        }

        dp_l[i] = cur_l;
        dp_c[i] = cur_c;

        if (cur_l > cur_max){
            cur_max = cur_l;
            cur_mcount = cur_c;
        } else if (cur_l == cur_max){
            cur_mcount += cur_c;
        }
    }

    printf("%d %d\n",cur_max,cur_mcount);
}

vector<int> readLine(){
    vector<int> array;
    int n;
    string line;
    if(getline(cin, line)) {
        istringstream sstr(line);
        while(sstr >> n){
            array.push_back(n);
        }
    }
    return array;
}

void problem1(){
    vector<int> array = readLine();
    LIS(array);
}

void problem2(){
    vector<int> v1 = readLine();
    vector<int> v2, fin;
    int max = *max_element(v1.begin(), v1.end());
    vector<int> table(max+1,0);
    for (int i : v1){
        table[i] = 1;
    }
    int n;
    string line;
    if(getline(cin, line)) {
        istringstream sstr(line);
        while(sstr >> n){
            if (table[n] == 1)
                v2.push_back(n);
        }
    }

    LCIS(v1,v1.size(),v2,v2.size());
}

int main(){

    int x;
    string end;

    cin >> x;
    getline(cin,end);
    switch (x){

        case 1:
            problem1();
            break;
        
        case 2:
            problem2();
            break;
    }

    return 0;
}