==============================
L2JDream - Instalação do Banco de Dados
L2JDream - Database Installation
==============================

📁 Pasta atual: /tools
Contém:
- install_db.bat (Windows)
- install_db.sh (Linux)
- /sql (pasta com todos os arquivos SQL)

============================================
🔧 Requisitos / Requirements
============================================
1. Ter o MariaDB ou MySQL instalado no sistema
2. Usuário e senha padrão:
   Usuário / User: root
   Senha / Password: root
3. Banco de dados a ser usado: l2jdb
4. Ter o MySQL no PATH do sistema (ou configurar manualmente no script)

============================================
🪟 Windows (.bat)
============================================
Passos:
1. Clique duas vezes no arquivo "install_db.bat"
2. Escolha:
   - [F] Para instalação completa (apaga e recria o banco de dados)
   - [S] Para instalação parcial (ignora dados dos personagens)
3. O script irá executar todos os arquivos .sql da pasta "sql"

💡 Caso necessário, edite o caminho do MariaDB em:
install_db.bat → linha: set mysqlBinPath=C:\Program Files\MariaDB 10.4\bin

============================================
🐧 Linux (.sh)
============================================
Passos:
1. Abra um terminal na pasta /tools
2. Dê permissão ao script:
   chmod +x install_db.sh
3. Execute:
   ./install_db.sh
4. Escolha o idioma e o tipo de instalação (Completa ou Parcial)
5. O script irá importar automaticamente todos os arquivos .sql da pasta "sql"

💡 O script usa por padrão:
   mysql -h localhost -u root -p l2jdb

Se necessário, edite o script para alterar usuário/senha.

============================================
❓ Problemas comuns / Common Issues
============================================

- ERRO: "mysql: command not found"
  ➤ Solução: Verifique se o MySQL/MariaDB está instalado e adicionado ao PATH.

- ERRO: Acesso negado para 'root'
  ➤ Solução: Altere a senha no script ou use um usuário com permissão.

- Nenhuma tabela é criada
  ➤ Solução: Verifique se os arquivos .sql estão realmente dentro da pasta /sql.

============================================
📬 Suporte
============================================
Caso tenha dificuldades, poste sua dúvida no fórum ou envie uma mensagem com:
- Print do erro
- Sistema operacional
- O que tentou fazer

Obrigado por usar o projeto L2JDream!
