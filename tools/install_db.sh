#!/bin/bash

# Escolha de idioma
echo ""
echo "Select language / Selecione o idioma:"
echo "1) English"
echo "2) Português"
read -p "Option: " lang

if [[ "$lang" == "2" ]]; then
    MSG_WELCOME="Instalação do Banco de Dados"
    MSG_OPTION="Opções: (f) instalação completa, (s) ignorar dados dos personagens, (q) sair"
    MSG_CONFIRM="Tem certeza que deseja APAGAR todos os dados, inclusive personagens? (s/n)"
    MSG_DONE="Instalação concluída!"
    MSG_ABORTED="Instalação cancelada."
    MSG_PROCESSING="Executando SQLs da pasta sql/..."
else
    MSG_WELCOME="Database Installation"
    MSG_OPTION="Options: (f) full install, (s) skip characters, (q) quit"
    MSG_CONFIRM="Are you sure to DELETE all data, including characters? (y/n)"
    MSG_DONE="Installation completed!"
    MSG_ABORTED="Installation aborted."
    MSG_PROCESSING="Running SQL files from sql/..."
fi

# Configuração do banco
MYSQL_PATH="mysql"
DB_USER="root"
DB_PASS="root"
DB_HOST="localhost"
DB_NAME="l2jdb"
SQL_DIR="sql"

echo ""
echo "=============================="
echo "   $MSG_WELCOME"
echo "=============================="
echo "$MSG_OPTION"
read -p "> " option

# Confirmar se é instalação completa
if [[ "$option" == "f" ]]; then
    read -p "$MSG_CONFIRM " confirm
    if [[ "$lang" == "2" && "$confirm" != "s" ]] || [[ "$lang" != "2" && "$confirm" != "y" ]]; then
        echo "$MSG_ABORTED"
        exit 1
    fi
    echo ""
    echo "$MSG_PROCESSING"
    
    # Executa todos os arquivos .sql (ordem alfabética)
    for file in "$SQL_DIR"/*.sql; do
        echo "Importando: $file"
        $MYSQL_PATH -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$file"
    done

elif [[ "$option" == "s" ]]; then
    echo ""
    echo "$MSG_PROCESSING"

    # Executa arquivos exceto os que contêm "character" no nome
    for file in "$SQL_DIR"/*.sql; do
        if [[ "$file" != *"character"* ]]; then
            echo "Importando: $file"
            $MYSQL_PATH -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$file"
        fi
    done

else
    echo "$MSG_ABORTED"
    exit 0
fi

echo ""
echo "$MSG_DONE"
