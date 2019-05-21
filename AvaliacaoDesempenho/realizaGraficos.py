import sys
import matplotlib.pyplot as plt
import numpy as np
import json

ficheiro = sys.argv[1]
label = sys.argv[2]

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

def leFicheiro(nome,label):
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

def criaGrafico(x,y1,y2,y3,label):
    plt.plot(x, y1)
    plt.plot(x, y2)
    plt.plot(x, y3)

    plt.legend(['mediana', 'percentil 95', 'percentil 99'], loc='upper left')

    plt.title(label)
    plt.xlabel('Nr. clientes concorrentes')
    plt.ylabel('Tempo de resposta (ms)')
    plt.show()


(x,y_m,y_95,y_99) = leFicheiro(ficheiro,label)
criaGrafico(x,y_m,y_95,y_99,label)
