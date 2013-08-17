package hudson.plugins.warnings.parser.gendarme;

//CHECKSTYLE:OFF
@SuppressWarnings("javadoc")
public class DotNetAssembly {
    private final String fullName;
    private String name;
    private String version;
    private String culture;
    private String publicKeyToken;

    public DotNetAssembly(final String fullName) {
        this.fullName = fullName;

        String[] splitted = this.fullName.split(",");
        int cpt = 0;
        for (String s : splitted) {
            if (cpt == 0) {
                name = s.trim();
            }
            else {
                String[] keyValue = s.trim().split("=");
                if (keyValue[0].equals("Version")) {
                    version = keyValue[1];
                }
                else if (keyValue[0].equals("Culture")) {
                    culture = keyValue[1];
                }
                else if (keyValue[0].equals("PublicKeyToken")) {
                    publicKeyToken = keyValue[1];
                }
            }
            cpt++;
        }
    }

    public String getFullName() {
        return fullName;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getCulture() {
        return culture;
    }

    public String getPublicKeyToken() {
        return publicKeyToken;
    }
}
