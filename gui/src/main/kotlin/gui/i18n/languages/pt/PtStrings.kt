package gui.i18n.languages.pt

import gui.i18n.AppStrings

object PtStrings : AppStrings {
    // Common & Generic Words
    override val appTitle = "ClustEngine Panel"
    override val cancelBtn = "Cancelar"
    override val confirmBtn = "Confirmar"
    override val backBtn = "Voltar"
    override val startBtn = "Iniciar"
    override val stopBtn = "Parar"
    override val deleteBtn = "Apagar"
    override val providerLabel = "Fornecedor:"
    override val instancesLabel = "Instâncias:"

    // Navigation & Sidebar
    override val savedClustersMenu = "Clusters Guardados"
    override val dashboardMenu = "Painel do Cluster"
    override val createClusterMenu = "Criar Cluster"
    override val openMenuDesc = "Abrir Menu"
    override val collapseSidebarDesc = "Recolher Menu Lateral"

    // SavedClustersScreen
    override val homeTitle = "Clusters Guardados"
    override val homeSubtitle = "Selecione um cluster para gerir ou crie um novo."
    override val savedClustersTitle = "Clusters Guardados"
    override val noClustersFoundTitle = "Nenhum cluster encontrado"
    override val createFirstClusterDesc = "Crie o seu primeiro cluster para começar."
    override val createNewClusterBtn = "Criar Novo Cluster"

    override fun clusterNodeCount(
        provider: String,
        size: Int,
    ): String {
        return "$provider • $size Nós"
    }

    // SetupScreen (Create Cluster)
    override val setupTitle = "Configuração do Cluster"
    override val clusterNameLabel = "Nome do Cluster"
    override val clusterNamePlaceholder = "ex: cluster-producao"
    override val instanceCountLabel = "Número de Instâncias"
    override val instanceCountPlaceholder = "ex: 3"
    override val cloudProviderTitle = "Fornecedor Cloud"
    override val initEngineBtn = "Inicializar Motor"
    override val deployingBtn = "A criar cluster..."
    override val goToClustersBtn = "Ver Clusters Guardados"
    override val instanceFlavorTitle = "Tipo de Instância / Flavor"
    override val selectFlavorPlaceholder = "Selecione um tipo de instância..."
    override val cancelClusterCreationBtn = "Cancelar Criação"
    override val confirmClusterCancelCreationTitle = "Cancelar Criação do Cluster?"
    override val confirmCancelCreationDesc =
        "Tem a certeza que deseja abortar o processo de creiação ? " +
            "Esta ação irá tentar limpar os recursos criados e não pode ser revertida."
    override val logCancelling = "A cancelar a criação do cluster e a limpar recursos..."

    // Cluster Details (Cluster View)
    override val dashboardTitle = "Painel ClustEngine"
    override val clusterManagement = "Gestão do Cluster"
    override val clusterDetailsSection = "Detalhes do Cluster"
    override val nodesInformationSection = "Informação dos Nós"
    override val initClusterBtn = "Inicializar Cluster"
    override val listInstancesBtn = "Listar Instâncias"
    override val deleteClusterBtn = "Apagar Cluster"
    override val clusterStatusSection = "Estado do Cluster"
    override val primaryNodeLabel = "Nó Principal"
    override val secondaryNodeLabel = "Nó Secundário"
    override val clusterActionsSection = "Ações do Cluster"
    override val viewDetailsContentDesc = "Ver Detalhes"

    // Dashboard Dialogs
    override val confirmStopClusterTitle = "Parar Cluster?"
    override val confirmStopClusterDesc = "Todos os nós serão suspensos. Tem a certeza de que pretende parar o cluster inteiro?"
    override val confirmDeleteClusterTitle = "Apagar Cluster?"
    override val confirmDeleteClusterDesc = "Esta ação é irreversível. O cluster e todos os seus dados serão destruídos. Continuar?"

    // InstanceDetailsScreen (Single Node View)
    override val backToDashboard = "Voltar ao Dashboard"
    override val nodeManagementTitle = "Gestão do Nó"
    override val instanceManagement = "Gestão de Instância (Individual)"
    override val instanceDetailsSection = "Detalhes da Instância"
    override val nodeActionsSection = "Ações do Nó"
    override val instanceIdLabel = "ID da Instância"
    override val instanceIdPlaceholder = "Introduza o ID numérico"
    override val publicIpLabel = "IP Público"
    override val privateIpLabel = "IP Privado"
    override val publicDnsLabel = "DNS Público"
    override val startNodeBtn = "Iniciar Nó"
    override val stopNodeBtn = "Parar Nó"
    override val terminateNodeBtn = "Terminar Nó"
    override val terminateBtn = "Terminar"

    // Instance Dialogs
    override val confirmStopInstanceTitle = "Parar Instância?"
    override val confirmStopInstanceDesc = "A instância será suspensa temporariamente. Confirmar paragem?"
    override val confirmDeleteInstanceTitle = "Apagar Instância?"
    override val confirmDeleteInstanceDesc = "A instância selecionada será permanentemente destruída. Confirmar ação?"
    override val terminateInstanceDialogTitle = "Terminar Instância"

    override fun terminateInstanceDialogDesc(id: String) =
        "Tem a certeza que deseja terminar permanentemente a instância $id? Esta ação é irreversível."

    // SettingsScreen
    override val settingsTitle = "Definições"
    override val themeLabel = "Tema da Aplicação"
    override val themeLight = "Modo Claro"
    override val themeDark = "Modo Escuro"
    override val themeAuto = "Automático (Sistema)"
    override val languageLabel = "Idioma"
    override val versionLabel = "Versão da Aplicação"
    override val docsLabel = "Documentação"
    override val tabGeneral = "Geral"
    override val selectFileDialogTitle = "Selecionar Ficheiro"
    override val filePath = "Caminho do Ficheiro"
    override val osImageId = "ID da Imagem"
    override val osAvailabilityZone = "Zona de Disponibilidade"
    override val osSecurityGroup = "Grupo de Segurança"
    override val osNetworkId = "ID da Rede"

    // Script
    override val tabScriptInjection = "Injeção de Scripts"
    override val saveSettingsBtn = "Guardar Configurações"
    override val confirmSaveSettingsTitle = "Guardar Definições?"
    override val confirmSaveSettingsDesc =
        "Tens a certeza que queres guardar estas configurações no ficheiro .env? Isto irá substituir as configurações existentes."
    override val baseScriptConfig = "Configuração do Script Base"
    override val userScriptConfig = "Configuração do Script de Utilizador"
    override val browseBtn = "Procurar"
    override val selectScriptDialogTitle = "Selecionar Ficheiro do Script"
    override val dirPath = "Caminho do Diretório"
    override val fileName = "Nome do Ficheiro"

    // Credentials
    override val tabCredentials = "Credenciais Cloud"
    override val scriptConfigSection = "Caminhos de Configuração"
    override val awsConfigSection = "Credenciais e Configuração AWS"
    override val openStackConfigSection = "Credenciais e Configuração OpenStack"
    override val awsRegion = "Região"
    override val keyFileName = "Nome do Ficheiro da Chave"
    override val keyFilePath = "Caminho do Ficheiro da Chave"
    override val awsSubnetId = "ID da Subnet"
    override val awsSecGroupId = "ID do Security Group"
    override val awsImageId = "ID da Imagem"
    override val osUserName = "Nome de Utilizador"
    override val osDomain = "Domínio"
    override val osPassword = "Palavra-passe"
    override val osProjectName = "Nome do Projeto"
    override val osBaseUrl = "URL Base"
    override val awsKeyFile = "Ficheiro da Chave AWS (.pem)"
    override val osKeyFile = "Ficheiro da Chave OpenStack (.pem)"

    // Terminal / Console
    override val terminalTitle = ">_ LOGS DO TERMINAL"
    override val terminalEmpty = "A aguardar ações..."
    override val consoleOutput = "Output da Consola"

    // Terminal Logs
    override fun logWelcome() = "Bem-vindo à GUI do ClustEngine."

    override fun logErrorProvider() = "Erro: Selecione um fornecedor."

    override fun logInitEngine(provider: String) = "A inicializar com $provider..."

    override fun logEngineReady() = "Motor inicializado. Pronto para receber comandos."

    override fun logInitCluster() = "A executar inicialização..."

    override fun logListInstances() = "A listar instâncias..."

    override fun logStartCluster() = "A iniciar cluster..."

    override fun logStopCluster() = "A parar cluster..."

    override fun logDeleteCluster() = "A apagar cluster..."

    override fun logStartInstance(id: Int) = "A iniciar instância ID: $id..."

    override fun logStopInstance(id: Int) = "A parar instância ID: $id..."

    override fun logDeleteInstance(id: Int) = "A apagar instância ID: $id..."

    override fun logInvalidId() = "Erro: ID inválido."

    override fun logGenericError(error: String) = "ERRO: $error"
}
