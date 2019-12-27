package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,
    GUI_ICONS_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,
    GEAR_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,
    DISPLAY_TOOLTIP,
    GEAR_TOOLTIP,
    CLASSIFICATION,
    CLUSTERING,
    ALGORITHM,
    CONFIGURATION,
    CAMBRIA,
    EDIT_ENTERED_DATA,
    NO_OF_INSTANCES,
    NO_OF_LABELS,
    LABELNAMES,
    RUN_ALGORITHM,
    META_DATA,
    EMPTY_FEILDS,
    INVALID_DATA,
    ZERO_VALUES,
    PRESS_RUN,
    PRESS_RUN_TEXT,
    CONTINUOUS_RUN,
    MAX_ITERATIONS,
    UPDATE_INTERVAL,
    MAX_ITERATIONS_REACHED,
    MAX_ITERATIONS_REACHED_TEXT,
    STRING_FORMAT,
    WARNING, 
    WARNING_TEXT,
    FORCE_CLOSE,
    CLUSTERS_NAME,
    
    /* file name for the css files    */
    CHARTCSS,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    UPDATEINTERVAL,
    MAXITERATIONS,
    CLUSTERS,
    ZEROVALUES,
    EMPTYTEXTFEILDS,
    SAVECONFIG,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    FILE_CHOOSER_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    DATA_FILE,
    DATA_VISUALISATION,
    DISTINCT_POINT_ERROR,
    TEN_LINE_DIALOG,
    NAME_ERROR
}
