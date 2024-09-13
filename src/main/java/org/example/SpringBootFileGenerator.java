package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

public class SpringBootFileGenerator {

    public static void main(String[] args) {
        String liquibaseChangeSet = """
                <changeSet id="1" author="author">
                    <createTable tableName="user_profile">
                        <column name="id" type="bigint">
                            <constraints primaryKey="true" nullable="false"/>
                        </column>
                        <column name="first_name" type="varchar(255)"/>
                        <column name="email" type="varchar(255)"/>
                    </createTable>
                </changeSet>
        """;

        String basePackage = "com.example";
        String basePath = "src/main/java";

        String tableName = null;
        String[] columnDetails = new String[0];

        try {
            // Parse the Liquibase changeset XML string
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(liquibaseChangeSet)));

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            // Extract table name and columns
            NodeList tableNodes = doc.getElementsByTagName("createTable");
            if (tableNodes.getLength() > 0) {
                Node tableNode = tableNodes.item(0);
                Element tableElement = (Element) tableNode;
                tableName = toCamelCase(tableElement.getAttribute("tableName"));

                NodeList columnNodes = tableElement.getElementsByTagName("column");
                columnDetails = new String[columnNodes.getLength() - 1];
                int index = 0;

                for (int i = 0; i < columnNodes.getLength(); i++) {
                    Node columnNode = columnNodes.item(i);
                    Element columnElement = (Element) columnNode;
                    String columnName = toCamelCase(columnElement.getAttribute("name"));
                    if ("id".equalsIgnoreCase(columnName)) {continue;}
                    String columnType = cleanDataType(columnElement.getAttribute("type"));
                    columnDetails[index++] = columnName + ":" + columnType;
                }

                System.out.println("------------------------------------------------");
                System.out.println("THANK YOU FOR USING SPRING BOOT FILE GENERATOR !");
                System.out.println("------------------------------------------------");
                System.out.println("The base package is : " + basePackage);
                System.out.println("The base path is : " + basePath);
                System.out.println("The table name is : " + tableName);
                System.out.println("The columns are : ");
                for (String column : columnDetails) {
                    System.out.println(column);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        generateFiles(tableName, columnDetails, basePackage, basePath);
    }

    public static String toCamelCase(String underscoreString) {
        if (underscoreString == null || underscoreString.isEmpty()) {
            return underscoreString;
        }

        StringBuilder camelCaseString = new StringBuilder();
        boolean nextCharUpperCase = false;

        for (char c : underscoreString.toCharArray()) {
            if (c == '_') {
                nextCharUpperCase = true;
            } else {
                if (nextCharUpperCase) {
                    camelCaseString.append(Character.toUpperCase(c));
                    nextCharUpperCase = false;
                } else {
                    camelCaseString.append(c);
                }
            }
        }
        return camelCaseString.toString();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String cleanDataType(String type) {
        // Extract the base type, e.g., varchar(255) -> varchar
        if (type != null && type.contains("(")) {
            return type.substring(0, type.indexOf('('));
        }
        return type;
    }

    private static String getColumnType(String columnDataType){
        return switch (columnDataType) {
            case "int" -> "Integer";
            case "long" -> "Long";
            case "boolean" -> "Boolean";
            case "double" -> "Double";
            case "string" -> "String";
            case "bigint" -> "Long";
            default -> "String"; // Default to String for simplicity
        };
    }

    private static void generateFiles(String tableName, String[] columns, String basePackage, String baseDirectoryPath){

        String basePath = baseDirectoryPath + "/" + basePackage.replace('.', '/');

        // Directory paths
        String serviceDir = basePath + "/service";
        String serviceImplDir = basePath + "/service/impl";
        String resourceDir = basePath + "/web/rest";
        String repositoryDir = basePath + "/repository";
        String mapperDir = basePath + "/mapper";
        String domainDir = basePath + "/domain";
        String dtoDir = basePath + "/service/dto";

        // Create directories if they do not exist
        createDirectoryIfNotExists(serviceDir);
        createDirectoryIfNotExists(serviceImplDir);
        createDirectoryIfNotExists(resourceDir);
        createDirectoryIfNotExists(repositoryDir);
        createDirectoryIfNotExists(mapperDir);
        createDirectoryIfNotExists(domainDir);
        createDirectoryIfNotExists(dtoDir);

        try {
            // Generate files
            writeToFile(domainDir + "/" + tableName + ".java", generateEntityContent(tableName, columns, basePackage));
            writeToFile(dtoDir + "/" + tableName + "DTO.java", generateDTOContent(tableName, columns, basePackage));
            writeToFile(serviceDir + "/" + tableName + "Service.java", generateServiceContent(tableName, basePackage));
            writeToFile(serviceImplDir + "/" + tableName + "ServiceImpl.java", generateServiceImplContent(tableName, basePackage));
            writeToFile(resourceDir + "/" + tableName + "Resource.java", generateResourceContent(tableName, basePackage));
            writeToFile(repositoryDir + "/" + tableName + "Repository.java", generateRepositoryContent(tableName, basePackage));
            writeToFile(mapperDir + "/" + tableName + "Mapper.java", generateMapperContent(tableName, basePackage));

            System.out.println("------------------------------------------------");
            System.out.println("FILES GENERATED SUCCESSFULLY !");
            System.out.println("------------------------------------------------");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory and any necessary parent directories
        }
    }

    private static void writeToFile(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    private static String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public static String lowerFirstChar(String input) {
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

    private static String generateEntityContent(String tableName, String[] columns, String basePackage) {
        String tableNameSnake = toSnakeCase(tableName);
        StringBuilder fields = new StringBuilder();
        StringBuilder gettersAndSetters = new StringBuilder();

        for (String column : columns) {
            String[] parts = column.split(":");
            String columnName = parts[0];
            String columnType = parts[1];

            // Adjust type mappings as needed
            columnType = getColumnType(columnType);

            String columnNameSnake = toSnakeCase(columnName);

            fields.append("    @Column(name = \"" + columnNameSnake + "\")\n");
            fields.append("    private ").append(columnType).append(" ").append(columnName).append(";\n\n");

            // Generate getter
            gettersAndSetters.append("    public ").append(columnType).append(" get").append(capitalize(columnName)).append("() {\n")
                    .append("        return ").append(columnName).append(";\n")
                    .append("    }\n\n");

            // Generate setter
            gettersAndSetters.append("    public void set").append(capitalize(columnName)).append("(").append(columnType).append(" ").append(columnName).append(") {\n")
                    .append("        this.").append(columnName).append(" = ").append(columnName).append(";\n")
                    .append("    }\n\n");
        }

        return "package " + basePackage + ".domain;\n\n" +
                "import jakarta.persistence.*;\n" +
                "import java.io.Serializable;\n\n\n" +
                "@Entity\n" +
                "@Table(name = \"" + tableNameSnake + "\")\n" +
                "public class " + tableName + " implements Serializable" + " {\n\n" +
                "    private static final long serialVersionUID = 1L;\n\n" +
                "    @Id\n" +
                "    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"sequenceGenerator\")\n" +
                "    @SequenceGenerator(name = \"sequenceGenerator\")\n" +
                "    private Long id;\n\n" +
                fields.toString() +
                gettersAndSetters.toString() +
                "}\n";
    }

    private static String generateDTOContent(String tableName, String[] columns, String basePackage) {
        StringBuilder fields = new StringBuilder();
        StringBuilder gettersAndSetters = new StringBuilder();

        for (String column : columns) {
            String[] parts = column.split(":");
            String columnName = parts[0];
            String columnType = parts[1];

            // Adjust type mappings as needed
            columnType = getColumnType(columnType);

            fields.append("    private ").append(columnType).append(" ").append(columnName).append(";\n\n");

            // Generate getter
            gettersAndSetters.append("    public ").append(columnType).append(" get").append(capitalize(columnName)).append("() {\n")
                    .append("        return ").append(columnName).append(";\n")
                    .append("    }\n\n");

            // Generate setter
            gettersAndSetters.append("    public void set").append(capitalize(columnName)).append("(").append(columnType).append(" ").append(columnName).append(") {\n")
                    .append("        this.").append(columnName).append(" = ").append(columnName).append(";\n")
                    .append("    }\n\n");
        }

        return "package " + basePackage + ".service.dto;\n\n" +
                "import java.io.Serializable;\n" +
                "public class " + tableName + "DTO implements Serializable {\n\n" +
                fields.toString() +
                "    private Long id;\n" +
                "    public Long getId() {return id;}\n" +
                "    public void setId(Long id) {this.id = id;}\n" +
                gettersAndSetters.toString() +
                "}\n";
    }

    private static String generateServiceContent(String tableName, String basePackage) {
        return "package " + basePackage + ".service;\n\n" +
                "import " + basePackage + ".service.dto." + tableName + "DTO;\n" +
                "import java.util.Optional;\n" +
                "import org.springframework.data.domain.Page;\n" +
                "import org.springframework.data.domain.Pageable;\n" +
                "public interface " + tableName + "Service {\n" +
                "    " + tableName + "DTO save(" + tableName + "DTO " + lowerFirstChar(tableName) + "dto);\n" +
                "    " + "Optional<" + tableName + "DTO> findOne(Long id);\n" +
                "    " + "Page<" + tableName + "DTO> findAll(Pageable pageable);\n" +
                "    " + tableName + "DTO update(" + tableName + "DTO " + lowerFirstChar(tableName) + "dto);\n" +
                "    " + "Optional<" + tableName + "DTO> partialUpdate(" + tableName + "DTO " + lowerFirstChar(tableName) + "dto);\n" +
                "    void delete(Long id);\n" +
                "}\n";
    }

    private static String generateServiceImplContent(String tableName, String basePackage) {
        return "package " + basePackage + ".service.impl;\n\n" +
                "import " + basePackage + ".repository." + tableName + "Repository;\n" +
                "import " + basePackage + ".service.dto." + tableName + "DTO;\n" +
                "import " + basePackage + ".service." + tableName + "Service;\n" +
                "import " + basePackage + ".mapper." + tableName + "Mapper;\n" +
                "import " + basePackage + ".domain." + tableName + ";\n" +
                "import org.springframework.stereotype.Service;\n" +
                "import org.springframework.transaction.annotation.Transactional;\n" +
                "import org.slf4j.Logger;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "import java.util.Optional;\n" +
                "import org.springframework.data.domain.Page;\n" +
                "import org.springframework.data.domain.Pageable;\n" +
                "@Service\n" +
                "@Transactional\n" +
                "public class " + tableName + "ServiceImpl implements " + tableName + "Service {\n\n" +

                "    private static final Logger LOG = LoggerFactory.getLogger(" + tableName + "ServiceImpl.class);\n" +

                "    private final " + tableName + "Repository " + lowerFirstChar(tableName) + "Repository;\n" +
                "    private final " + tableName + "Mapper " + lowerFirstChar(tableName) + "Mapper;\n\n" +
                "    public " + tableName + "ServiceImpl(" + tableName + "Repository " + lowerFirstChar(tableName) + "Repository, " + tableName + "Mapper " + lowerFirstChar(tableName) + "Mapper) {\n" +
                "        this." + lowerFirstChar(tableName) + "Repository = " + lowerFirstChar(tableName) + "Repository;\n" +
                "        this." + lowerFirstChar(tableName) + "Mapper = " + lowerFirstChar(tableName) + "Mapper;\n" +
                "    }\n\n" +

                "    @Override\n" +
                "    public " + tableName + "DTO save(" + tableName + "DTO " + lowerFirstChar(tableName) + "DTO) {\n" +
                "        LOG.debug(\"Request to save " + tableName + " : {}\"," + lowerFirstChar(tableName) + "DTO);\n" +
                "        " + tableName + " " + lowerFirstChar(tableName) + " = " + lowerFirstChar(tableName) + "Mapper.toEntity(" + lowerFirstChar(tableName) + "DTO);\n" +
                "        " + lowerFirstChar(tableName) + " = " + lowerFirstChar(tableName) + "Repository.save(" + lowerFirstChar(tableName) + ");\n" +
                "        return " + lowerFirstChar(tableName) + "Mapper.toDto(" + lowerFirstChar(tableName) + ");\n" +
                "    }\n\n" +

                "    @Override\n" +
                "    public " + tableName + "DTO update(" + tableName + "DTO " + lowerFirstChar(tableName) + "DTO) {\n" +
                "        LOG.debug(\"Request to update " + tableName + " : {}\"," + lowerFirstChar(tableName) + "DTO);\n" +
                "        " + tableName + " " + lowerFirstChar(tableName) + " = " + lowerFirstChar(tableName) + "Mapper.toEntity(" + lowerFirstChar(tableName) + "DTO);\n" +
                "        " + lowerFirstChar(tableName) + " = " + lowerFirstChar(tableName) + "Repository.save(" + lowerFirstChar(tableName) + ");\n" +
                "        return " + lowerFirstChar(tableName) + "Mapper.toDto(" + lowerFirstChar(tableName) + ");\n" +
                "    }\n\n" +

                "    @Override\n" +
                "    public Optional<" + tableName + "DTO> partialUpdate(" + tableName + "DTO " + lowerFirstChar(tableName) + "dto) {\n" +
                "        LOG.debug(\"Request to partially update " + tableName + " : {}\"," + lowerFirstChar(tableName) + "dto);\n" +
                "        return " + lowerFirstChar(tableName) + "Repository\n" +
                "            .findById(" + lowerFirstChar(tableName) + "dto.getId())\n" +
                "            .map(existing" + tableName + " -> {\n" +
                "                " + lowerFirstChar(tableName) + "Mapper.partialUpdate(existing" + tableName + ", " + lowerFirstChar(tableName) + "dto);\n" +
                "                return existing" + tableName + ";\n" +
                "            })\n" +
                "            .map(" + lowerFirstChar(tableName) + "Repository::save)\n" +
                "            .map(" + lowerFirstChar(tableName) + "Mapper::toDto);\n" +
                "    }\n\n" +

                "    @Override\n" +
                "    @Transactional(readOnly = true)\n" +
                "    public Page<" + tableName + "DTO> findAll(Pageable pageable) {\n" +
                "        LOG.debug(\"Request to get all " + tableName + "s\");\n" +
                "        return " + lowerFirstChar(tableName) + "Repository.findAll(pageable).map(" + lowerFirstChar(tableName) + "Mapper::toDto);\n" +
                "    }\n\n" +

                "    @Override\n" +
                "    @Transactional(readOnly = true)\n" +
                "    public Optional<" + tableName + "DTO> findOne(Long id) {\n" +
                "        LOG.debug(\"Request to get " + tableName + " : {}\", id);\n" +
                "        return " + lowerFirstChar(tableName) + "Repository.findById(id).map(" + lowerFirstChar(tableName) + "Mapper::toDto);\n" +
                "    }\n" +

                "    @Override\n" +
                "    public void delete(Long id) {\n" +
                "        LOG.debug(\"Request to delete " + tableName + " : {}\", id);\n" +
                "        " + lowerFirstChar(tableName) + "Repository.deleteById(id);\n" +
                "    }\n" +

                "}\n";
    }

    private static String generateResourceContent(String tableName, String basePackage) {
        return "package " + basePackage + ".web.rest;\n\n" +
                "import " + basePackage + ".service.dto." + tableName + "DTO;\n" +
                "import " + basePackage + ".service." + tableName + "Service;\n" +
                "import " + basePackage + ".repository." + tableName + "Repository;\n" +
                "import org.springframework.http.ResponseEntity;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "import " + basePackage + ".web.rest.errors.BadRequestAlertException;\n" +
                "import org.slf4j.Logger;\n" +
                "import org.springframework.beans.factory.annotation.Value;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "import java.net.URI;\n" +
                "import java.net.URISyntaxException;\n" +
                "import java.util.*;\n" +
                "import tech.jhipster.web.util.*;\n" +
                "import org.springframework.data.domain.Page;\n" +
                "import org.springframework.data.domain.Pageable;\n"+
                "import org.springframework.http.HttpHeaders;\n\n"+
                "import org.springframework.web.servlet.support.ServletUriComponentsBuilder;\n" +
                "import java.util.List;\n\n" +

                "@RestController\n" +
                "@RequestMapping(\"/api/" + lowerFirstChar(tableName) + "\")\n" +

                "public class " + tableName + "Resource {\n\n" +
                "    private static final Logger LOG = LoggerFactory.getLogger(" + tableName + "Resource.class);\n" +
                "    private static final String ENTITY_NAME = \"" + lowerFirstChar(tableName) + "\";\n\n" +

                "  @Value(\"${jhipster.clientApp.name}\")\n" +
                "    private String applicationName;\n\n" +

                "    private final " + tableName + "Service " + lowerFirstChar(tableName) + "Service;\n" +
                "    private final " + tableName + "Repository " + lowerFirstChar(tableName) + "Repository;\n" +

                "    public " + tableName + "Resource(" + tableName + "Repository " + lowerFirstChar(tableName) + "Repository, " +  tableName + "Service " + lowerFirstChar(tableName) + "Service) {\n" +
                "        this." + lowerFirstChar(tableName) + "Repository = " + lowerFirstChar(tableName) + "Repository;\n" +
                "        this." + lowerFirstChar(tableName) + "Service = " + lowerFirstChar(tableName) + "Service;\n" +
                "    }\n\n" +

                "   /**\n" +
                "     * {@code POST  /" + lowerFirstChar(tableName) +"} : Create a new " + lowerFirstChar(tableName) +".\n" +
                "     *\n" +
                "     * @param " + lowerFirstChar(tableName) +"DTO the " + lowerFirstChar(tableName) +"DTO to create.\n" +
                "     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new " + lowerFirstChar(tableName) +"DTO, or with status {@code 400 (Bad Request)} if the " + lowerFirstChar(tableName) +" has already an ID.\n" +
                "     * @throws URISyntaxException if the Location URI syntax is incorrect.\n" +
                "     */\n\n" +

                "    @PostMapping(\"\")\n" +
                "    public ResponseEntity<" + tableName + "DTO> create(@RequestBody " + tableName + "DTO " + lowerFirstChar(tableName) + "DTO) throws URISyntaxException {\n" +
                "        LOG.debug(\"REST request to save " + tableName + " : {}\", " + lowerFirstChar(tableName) + "DTO);\n" +
                "        if (" + lowerFirstChar(tableName) + "DTO.getId() != null) {\n" +
                "            throw new BadRequestAlertException(\"A new " + lowerFirstChar(tableName) + " cannot already have an ID\", ENTITY_NAME, \"idexists\");\n" +
                "        }\n" +
                "        " + lowerFirstChar(tableName) + "DTO = " + lowerFirstChar(tableName) + "Service.save(" + lowerFirstChar(tableName) + "DTO);\n" +
                "        return ResponseEntity.created(new URI(\"/api/" + lowerFirstChar(tableName) + "s/\" + " + lowerFirstChar(tableName) + "DTO.getId()))\n" +
                "            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, " + lowerFirstChar(tableName) + "DTO.getId().toString()))\n" +
                "            .body(" + lowerFirstChar(tableName) + "DTO);\n" +
                "    }\n\n" +

                "   /**\n" +
                "     * {@code PUT  /" + lowerFirstChar(tableName) +"/:id} : Updates an existing " + lowerFirstChar(tableName) +".\n" +
                "     *\n" +
                "     * @param id the id of the " + lowerFirstChar(tableName) +"DTO to save.\n" +
                "     * @param " + lowerFirstChar(tableName) + "DTO the " + lowerFirstChar(tableName) +"DTO to update.\n" +
                "     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated " + lowerFirstChar(tableName) +"DTO,\n" +
                "     * or with status {@code 400 (Bad Request)} if the " + lowerFirstChar(tableName) +"DTO is not valid,\n" +
                "     * or with status {@code 500 (Internal Server Error)} if the " + lowerFirstChar(tableName) +"DTO couldn't be updated.\n" +
                "     * @throws URISyntaxException if the Location URI syntax is incorrect.\n" +
                "     */\n"+

                "    @PutMapping(\"/{id}\")\n" +
                "    public ResponseEntity<" + tableName + "DTO> update" + tableName + "(@PathVariable(value = \"id\", required = false) final Long id, @RequestBody " + tableName + "DTO " +  lowerFirstChar(tableName) + "DTO) {\n\n" +

                "        LOG.debug(\"REST request to update " + tableName + " : {}, {}\", id, " + lowerFirstChar(tableName) + "DTO);\n\n" +

                "        if (" + lowerFirstChar(tableName) + "DTO.getId() == null) {\n" +
                "            throw new BadRequestAlertException(\"Invalid id\", ENTITY_NAME, \"idnull\");\n" +
                "        }\n" +

                "        if (!Objects.equals(id, " + lowerFirstChar(tableName) + "DTO.getId())) {\n" +
                "            throw new BadRequestAlertException(\"Invalid id\", ENTITY_NAME, \"idinvalid\");\n" +
                "        }\n" +

                "        if (!" + lowerFirstChar(tableName) + "Repository.existsById(id)) {\n" +
                "            throw new BadRequestAlertException(\"Entity Not Found\", ENTITY_NAME, \"idnotfound\");\n" +
                "        }\n\n" +

                "        " + tableName + "DTO result = " + lowerFirstChar(tableName) + "Service.update(" + lowerFirstChar(tableName) + "DTO);\n" +
                "        return ResponseEntity.ok()\n" +
                "            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, " + lowerFirstChar(tableName) + "DTO.getId().toString()))\n" +
                "            .body(result);\n" +
                "    }\n\n" +

                "   /**\n" +
                "     * {@code PATCH  /" + lowerFirstChar(tableName) + "/:id} : Partial updates given " + lowerFirstChar(tableName) + " of an existing " + lowerFirstChar(tableName) + ", " + lowerFirstChar(tableName) + " will ignore if it is null\n" +
                "     *\n" +
                "     * @param id the id of the " + lowerFirstChar(tableName) + "DTO to save.\n" +
                "     * @param " + lowerFirstChar(tableName) + "DTO the " + lowerFirstChar(tableName) + "DTO to update.\n" +
                "     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated " + lowerFirstChar(tableName) + "DTO,\n" +
                "     * or with status {@code 400 (Bad Request)} if the " + lowerFirstChar(tableName) + "DTO is not valid,\n" +
                "     * or with status {@code 404 (Not Found)} if the " + lowerFirstChar(tableName) + "DTO is not found,\n" +
                "     * or with status {@code 500 (Internal Server Error)} if the " + lowerFirstChar(tableName) + "DTO couldn't be updated.\n" +
                "     * @throws URISyntaxException if the Location URI syntax is incorrect.\n" +
                "     */\n\n" +

                "    @PatchMapping(value = \"/{id}\", consumes = { \"application/json\", \"application/merge-patch+json\" })\n" +
                "    public ResponseEntity<" + tableName + "DTO> partialUpdate" + tableName + "(\n" +
                "    @PathVariable(value = \"id\", required = false) final Long id,\n" +
                "    @RequestBody " + tableName + "DTO " + lowerFirstChar(tableName) + "DTO)" +
                "    throws URISyntaxException {\n\n" +

                "        LOG.debug(\"REST request to partially update " + tableName + " : {}, {}\", id, " + lowerFirstChar(tableName) + "DTO);\n\n" +

                "        if (" + lowerFirstChar(tableName) + "DTO.getId() == null) {\n" +
                "            throw new BadRequestAlertException(\"Invalid id\", ENTITY_NAME, \"idnull\");\n" +
                "        }\n" +

                "        if (!Objects.equals(id, " + lowerFirstChar(tableName) + "DTO.getId())) {\n" +
                "            throw new BadRequestAlertException(\"Invalid id\", ENTITY_NAME, \"idinvalid\");\n" +
                "        }\n" +

                "        if (!" + lowerFirstChar(tableName) + "Repository.existsById(id)) {\n" +
                "            throw new BadRequestAlertException(\"Entity Not Found\", ENTITY_NAME, \"idnotfound\");\n" +
                "        }\n\n" +

                "        Optional<" + tableName + "DTO> result = " + lowerFirstChar(tableName) + "Service.partialUpdate(" + lowerFirstChar(tableName) + "DTO);\n\n" +
                "        return ResponseUtil.wrapOrNotFound(\n" +
                "            result,\n" +
                "            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, " + lowerFirstChar(tableName) + "DTO.getId().toString())\n" +
                "        );\n" +
                "    }\n\n" +

                "/**\n" +
                "     * {@code GET  /" + lowerFirstChar(tableName) + "s} : get all the " + lowerFirstChar(tableName) + "s.\n" +
                "     *\n" +
                "     * @param pageable the pagination information.\n" +
                "     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of " + lowerFirstChar(tableName) + " in body.\n" +
                "     */\n" +

                "    @GetMapping(\"\")\n" +
                "    public ResponseEntity<List<" + tableName + "DTO>> getAll" + tableName + "s(Pageable pageable) {\n\n" +
                "        LOG.debug(\"REST request to get all " + tableName + "s\");\n\n" +
                "        Page<" + tableName + "DTO> page = " + lowerFirstChar(tableName) + "Service.findAll(pageable);\n" +
                "        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);\n\n" +
                "        return ResponseEntity.ok().headers(headers).body(page.getContent());\n" +
                "    }\n" +

                "   /**\n" +
                "     * {@code GET  /" + lowerFirstChar(tableName) + "/:id} : get the \"id\" " + lowerFirstChar(tableName) + ".\n" +
                "     *\n" +
                "     * @param id the id of the " + lowerFirstChar(tableName) + "DTO to retrieve.\n" +
                "     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the " + lowerFirstChar(tableName) + "DTO, or with status {@code 404 (Not Found)}.\n" +
                "     */\n\n" +

                "    @GetMapping(\"/{id}\")\n" +
                "    public ResponseEntity<" + tableName + "DTO> get" + tableName + "(@PathVariable(\"id\") Long id) {\n\n" +
                "        LOG.debug(\"REST request to get " + tableName + " : {}\", id);\n\n" +
                "        Optional<" + tableName + "DTO> " + lowerFirstChar(tableName) + "DTO = " + lowerFirstChar(tableName) + "Service.findOne(id);\n\n" +
                "        return ResponseUtil.wrapOrNotFound(" + lowerFirstChar(tableName) + "DTO);\n" +
                "    }\n\n" +


                "   /**\n" +
                "     * {@code DELETE  /\" + lowerFirstChar(tableName) + \"/:id} : delete the \"id\" \" + lowerFirstChar(tableName) + \".\n" +
                "     *\n" +
                "     * @param id the id of the \" + lowerFirstChar(tableName) + \"DTO to delete.\n" +
                "     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.\n" +
                "     */\n" +
                "    @DeleteMapping(\"/{id}\")\n" +
                "    public ResponseEntity<Void> delete" + tableName + "(@PathVariable(\"id\") Long id) {\n\n" +
                "        LOG.debug(\"REST request to delete " + tableName + " : {}\", id);\n\n" +
                "        " + lowerFirstChar(tableName) + "Service.delete(id);\n\n" +
                "        return ResponseEntity.noContent()\n" +
                "            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))\n" +
                "            .build();\n" +
                "    }\n" +

                "}\n";
    }

    private static String generateRepositoryContent(String tableName, String basePackage) {
        return "package " + basePackage + ".repository;\n\n" +
                "import " + basePackage + ".domain." + tableName + ";\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n" +
                "@Repository\n" +
                "public interface " + tableName + "Repository extends JpaRepository<" + tableName + ", Long> {}";
    }

    private static String generateMapperContent(String tableName, String basePackage) {
        return "package " + basePackage + ".mapper;\n\n" +
                "import " + basePackage + ".domain." + tableName + ";\n" +
                "import " + basePackage + ".service.dto." + tableName + "DTO;\n" +
                "import " + basePackage + ".service.mapper.EntityMapper;\n" +
                "import org.mapstruct.Mapper;\n\n" +
                "@Mapper(componentModel = \"spring\")\n" +
                "public interface " + tableName + "Mapper extends EntityMapper<" + tableName
                + "DTO, " + tableName + "> {}" ;
    }
}
