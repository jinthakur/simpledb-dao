# Lib usage example #

## Model object ##
```
@Entity(name = "SomeDomain")
public class Document implements SimpleDBEntity<String> {

    private String id;

    private Strinf name;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "n") 
    public String getName {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```

## Client DAO ##

```
public class DocumentDAO extends GenericDAOSimpleDB<Document, String> {

    @Override
    protected Class<Document> getEntityClass() {
        return Document.class;
    }
    
    @Override
    protected String getAccessKey() {
        // return accessKey;
    }

    @Override
    protected String getSecretKey() {
        // return secretKey;
    }

    // Some other specific DAO methods

}
```

## Usage ##
```
    @Autowired
    private DocumentDAO documentDAO;
    
    // Get document by id
    Document doc = documentDAO.getById("285");
    doc.setId("286");
    // Saves changes to SimpleDB
    documentDAO.saveOrUpdate(doc);
    // Get it again with new id
    doc = documentDAO.getById("286");
    // Delete document
    documentDAO.delete(doc);
    // Getting again, will be null
    Document doc2 = documentDAO.getById("286");
    
    // Will return number of Marks in domain
    Integer a1 = documentDAO.countRows("n = 'Mark'");
    // You can use both simple db column names and your model property names
    Integer a2 = documentDAO.countRows("name = 'Mark'");

    // Will return number of items in domain
    Integer b = documentDAO.countRows();
 
    // Will return 30 items (or less)
    Result<Document> res = documentDAO.getPortion(30, null);

    // check if there are more items in domain
    if (res.getNextToken() != null) {
        // Get next 30 
        Result<Document> res2 = documentDAO.getPortion(30, res.getNextToken());
    }
```