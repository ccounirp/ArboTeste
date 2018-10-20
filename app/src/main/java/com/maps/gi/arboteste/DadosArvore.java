package com.maps.gi.arboteste;

public class DadosArvore {

    //Dados da Árvore
    String nomePopular;
    String nomeCientifico;
    float cpa; //Circunferência a altura do peito
    float alturaTronco;
    float alturaCopa;

    //Informações da Raiz
    int notaRaiz; // nota da Raiz - Exposta (Nota 4 a 1) - Não exposta (Nota 5)
    Boolean danosNaCalcada; // Se houve danos na calçada ou não

    //Informações do Caule
    int notaCaule; // nota do Caule(Tronco)

    //Informações da Copa
    Boolean copaNormal;
    Boolean copaMediamenteDeformada;
    Boolean copaDeformada;
    int vigorDaCopa; //Nota de 5 a 1
    Boolean conflitoComRedeEletrica;

    //Informações da Árvore
    int notaVitalidadeDaArvore; //Nota de 5 a 1
    int notaDoencasPragasParasitas; // Nota de 3 a 1
    String observacoes;

    //A condição da árvore será a soma das notas.
    // Boa - 23 a  14
    // Regular - 13 a 6
    // Ruim - 5



}
