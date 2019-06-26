import sys
import matplotlib.pyplot as plt
import numpy as np
import json

args = len(sys.argv)

ficheiros = []
for i in range(1,args-1):
    ficheiros.append(sys.argv[i])

label = sys.argv[args-1]

def read(nome):
    """
    Método que le um ficheiro do armazenamento local.
    Devolve um objeto JSON com o username, a timeline e a lista dos utilizadores que o utilizador atual segue.
    """
    
    data = {}
    try:
        with open(nome, 'r') as f:
            data = json.load(f)

    except FileNotFoundError:
        pass
    finally:
        return data

def leFicheiro(nome):
    dados = read(nome)
    x = []
    y_m = []
    y_95 = []
    y_99 = []
    chaves = [int(i) for i in dados.keys()]
    for k in sorted(chaves):
        x.append(k)
        aux = np.array(dados[str(k)])
        pc = np.percentile(aux, [50,95,99])
        y_m.append(pc[0])
        y_95.append(pc[1])
        y_99.append(pc[2])
    
    return (x,y_m,y_95,y_99)

def criaGrafico(x,y,label):
    for i in y:
        plt.plot(x,i)
    
    legend = []
    for i in range(0,len(y)):
        legend.append(str(i+1) + 'ª versao')

    plt.legend(legend, loc='upper left')

    plt.title(label)
    plt.xlabel('Nr. clientes concorrentes')
    plt.ylabel('Tempo de resposta (ms)')
    plt.show()

y_ms = []
y_95s = []
y_99s = []
for fich in ficheiros:
    (x,y_m,y_95,y_99) = leFicheiro(fich)
    y_ms.append(y_m)
    y_95s.append(y_95)
    y_99s.append(y_99)

criaGrafico(x,y_ms,label+' (mediana)')
criaGrafico(x,y_95s,label+' (percentil_95)')
criaGrafico(x,y_99s,label+' (percentil_99)')

