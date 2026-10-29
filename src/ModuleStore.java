import java.util.*;

public class ModuleStore {
    private static List<Module> moduleList = new ArrayList<>();
    private static Map<String, Module> moduleMap = new HashMap<>();

    public static void setModules(List<Module> modules) {
        moduleList = new ArrayList<>(modules);
        moduleMap.clear();
        for (Module m : modules) {
            moduleMap.put(m.getModuleId(), m);
        }
    }

    public static List<Module> getAllModules() {
        return moduleList;
    }

    public static Module getModuleById(String moduleId) {
        return moduleMap.get(moduleId);
    }

    public static void updateStatus(String moduleId, String newStatus) {
        Module m = moduleMap.get(moduleId);
        if (m != null) {
            m.setStatus(newStatus);
        }
    }

    public static void clear() {
        moduleList.clear();
        moduleMap.clear();
    }

    public static void addModule(Module m) {
        moduleList.add(m);
        moduleMap.put(m.getModuleId(), m);
    }
}

