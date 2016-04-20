package edu.usc.cssl.tacit.crawlers.stackexchange.services;

import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.AnswerItem;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.CommentItem;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Item;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.QuestionItem;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StackExchangeSite {

    /* Statics last update 14.03.2014 */
    public static final String STACK_OVERFLOW                           = "stackoverflow";
    public static final String SERVER_FAULT                             = "serverfault";
    public static final String SUPER_USER                               = "superuser";
    public static final String META_STACK_OVERFLOW                      = "meta.stackoverflow";
    public static final String WEB_APPLICATIONS                         = "webapps";
    public static final String WEB_APPLICATIONS_META                    = "meta.webapps";
    public static final String ARQADE                                   = "gaming";
    public static final String ARQADE_META                              = "meta.gaming";
    public static final String WEBMASTERS                               = "webmasters";
    public static final String WEBMASTERS_META                          = "meta.webmasters";
    public static final String SEASONED_ADVICE                          = "cooking";
    public static final String SEASONED_ADVICE_META                     = "meta.cooking";
    public static final String GAME_DEVELOPMENT                         = "gamedev";
    public static final String GAME_DEVELOPMENT_META                    = "meta.gamedev";
    public static final String PHOTOGRAPHY                              = "photo";
    public static final String PHOTOGRAPHY_META                         = "meta.photo";
    public static final String CROSS_VALIDATED                          = "stats";
    public static final String CROSS_VALIDATED_META                     = "meta.stats";
    public static final String MATHEMATICS                              = "math";
    public static final String MATHEMATICS_META                         = "meta.math";
    public static final String HOME_IMPROVEMENT                         = "diy";
    public static final String HOME_IMPROVEMENT_META                    = "meta.diy";
    public static final String META_SUPER_USER                          = "meta.superuser";
    public static final String META_SERVER_FAULT                        = "meta.serverfault";
    public static final String GEOGRAPHIC_INFORMATION_SYSTEMS           = "gis";
    public static final String GEOGRAPHIC_INFORMATION_SYSTEMS_META      = "meta.gis";
    public static final String TEX_LATEX                                = "tex";
    public static final String TEX_LATEX_META                           = "meta.tex";
    public static final String ASK_UBUNTU                               = "askubuntu";
    public static final String ASK_UBUNTU_META                          = "meta.askubuntu";
    public static final String PERSONAL_FINANCE_MONEY                   = "money";
    public static final String PERSONAL_FINANCE_MONEY_META              = "meta.money";
    public static final String ENGLISH_LANGUAGE_USAGE                   = "english";
    public static final String ENGLISH_LANGUAGE_USAGE_META              = "meta.english";
    public static final String STACK_APPS                               = "stackapps";
    public static final String USER_EXPERIENCE                          = "ux";
    public static final String USER_EXPERIENCE_META                     = "meta.ux";
    public static final String UNIX_LINUX                               = "unix";
    public static final String UNIX_LINUX_META                          = "meta.unix";
    public static final String WORDPRESS_DEVELOPMENT                    = "wordpress";
    public static final String WORDPRESS_DEVELOPMENT_META               = "meta.wordpress";
    public static final String THEORETICAL_COMPUTER_SCIENCE             = "cstheory";
    public static final String THEORETICAL_COMPUTER_SCIENCE_META        = "meta.cstheory";
    public static final String ASK_DIFFERENT                            = "apple";
    public static final String ASK_DIFFERENT_META                       = "meta.apple";
    public static final String ROLE_PLAYING_GAMES                       = "rpg";
    public static final String ROLE_PLAYING_GAMES_META                  = "meta.rpg";
    public static final String BICYCLES                                 = "bicycles";
    public static final String BICYCLES_META                            = "meta.bicycles";
    public static final String PROGRAMMERS                              = "programmers";
    public static final String PROGRAMMERS_META                         = "meta.programmers";
    public static final String ELECTRICAL_ENGINEERING                   = "electronics";
    public static final String ELECTRICAL_ENGINEERING_META              = "meta.electronics";
    public static final String ANDROID_ENTHUSIASTS                      = "android";
    public static final String ANDROID_ENTHUSIASTS_META                 = "meta.android";
    public static final String BOARD_CARD_GAMES                         = "boardgames";
    public static final String BOARD_CARD_GAMES_META                    = "meta.boardgames";
    public static final String PHYSICS                                  = "physics";
    public static final String PHYSICS_META                             = "meta.physics";
    public static final String HOMEBREWING                              = "homebrew";
    public static final String HOMEBREWING_META                         = "meta.homebrew";
    public static final String INFORMATION_SECURITY                     = "security";
    public static final String INFORMATION_SECURITY_META                = "meta.security";
    public static final String WRITERS                                  = "writers";
    public static final String WRITERS_META                             = "meta.writers";
    public static final String VIDEO_PRODUCTION                         = "video";
    public static final String VIDEO_PRODUCTION_META                    = "meta.video";
    public static final String GRAPHIC_DESIGN                           = "graphicdesign";
    public static final String GRAPHIC_DESIGN_META                      = "meta.graphicdesign";
    public static final String DATABASE_ADMINISTRATORS                  = "dba";
    public static final String DATABASE_ADMINISTRATORS_META             = "meta.dba";
    public static final String SCIENCE_FICTION_FANTASY                  = "scifi";
    public static final String SCIENCE_FICTION_FANTASY_META             = "meta.scifi";
    public static final String CODE_REVIEW                              = "codereview";
    public static final String CODE_REVIEW_META                         = "meta.codereview";
    public static final String PROGRAMMING_PUZZLES_CODE_GOLF            = "codegolf";
    public static final String PROGRAMMING_PUZZLES_CODE_GOLF_META       = "meta.codegolf";
    public static final String QUANTITATIVE_FINANCE                     = "quant";
    public static final String QUANTITATIVE_FINANCE_META                = "meta.quant";
    public static final String PROJECT_MANAGEMENT                       = "pm";
    public static final String PROJECT_MANAGEMENT_META                  = "meta.pm";
    public static final String SKEPTICS                                 = "skeptics";
    public static final String SKEPTICS_META                            = "meta.skeptics";
    public static final String PHYSICAL_FITNESS                         = "fitness";
    public static final String PHYSICAL_FITNESS_META                    = "meta.fitness";
    public static final String DRUPAL_ANSWERS                           = "drupal";
    public static final String DRUPAL_ANSWERS_META                      = "meta.drupal";
    public static final String MOTOR_VEHICLE_MAINTENANCE_REPAIR         = "mechanics";
    public static final String MOTOR_VEHICLE_MAINTENANCE_REPAIR_META    = "meta.mechanics";
    public static final String PARENTING                                = "parenting";
    public static final String PARENTING_META                           = "meta.parenting";
    public static final String SHAREPOINT                               = "sharepoint";
    public static final String SHAREPOINT_META                          = "meta.sharepoint";
    public static final String MUSICAL_PRACTICE_PERFORMANCE             = "music";
    public static final String MUSICAL_PRACTICE_PERFORMANCE_META        = "meta.music";
    public static final String SOFTWARE_QUALITY_ASSURANCE_TESTING       = "sqa";
    public static final String SOFTWARE_QUALITY_ASSURANCE_TESTING_META  = "meta.sqa";
    public static final String MI_YODEYA                                = "judaism";
    public static final String MI_YODEYA_META                           = "meta.judaism";
    public static final String GERMAN_LANGUAGE                          = "german";
    public static final String GERMAN_LANGUAGE_META                     = "meta.german";
    public static final String JAPANESE_LANGUAGE                        = "japanese";
    public static final String JAPANESE_LANGUAGE_META                   = "meta.japanese";
    public static final String PHILOSOPHY                               = "philosophy";
    public static final String PHILOSOPHY_META                          = "meta.philosophy";
    public static final String GARDENING_LANDSCAPING                    = "gardening";
    public static final String GARDENING_LANDSCAPING_META               = "meta.gardening";
    public static final String TRAVEL                                   = "travel";
    public static final String TRAVEL_META                              = "meta.travel";
    public static final String PERSONAL_PRODUCTIVITY                    = "productivity";
    public static final String PERSONAL_PRODUCTIVITY_META               = "meta.productivity";
    public static final String CRYPTOGRAPHY                             = "crypto";
    public static final String CRYPTOGRAPHY_META                        = "meta.crypto";
    public static final String SIGNAL_PROCESSING                        = "dsp";
    public static final String SIGNAL_PROCESSING_META                   = "meta.dsp";
    public static final String FRENCH_LANGUAGE                          = "french";
    public static final String FRENCH_LANGUAGE_META                     = "meta.french";
    public static final String CHRISTIANITY                             = "christianity";
    public static final String CHRISTIANITY_META                        = "meta.christianity";
    public static final String BITCOIN                                  = "bitcoin";
    public static final String BITCOIN_META                             = "meta.bitcoin";
    public static final String LINGUISTICS                              = "linguistics";
    public static final String LINGUISTICS_META                         = "meta.linguistics";
    public static final String BIBLICAL_HERMENEUTICS                    = "hermeneutics";
    public static final String BIBLICAL_HERMENEUTICS_META               = "meta.hermeneutics";
    public static final String HISTORY                                  = "history";
    public static final String HISTORY_META                             = "meta.history";
    public static final String LEGO_ANSWERS                             = "bricks";
    public static final String LEGO_ANSWERS_META                        = "meta.bricks";
    public static final String SPANISH_LANGUAGE                         = "spanish";
    public static final String SPANISH_LANGUAGE_META                    = "meta.spanish";
    public static final String COMPUTATIONAL_SCIENCE                    = "scicomp";
    public static final String COMPUTATIONAL_SCIENCE_META               = "meta.scicomp";
    public static final String MOVIES_TV                                = "movies";
    public static final String MOVIES_TV_META                           = "meta.movies";
    public static final String CHINESE_LANGUAGE                         = "chinese";
    public static final String CHINESE_LANGUAGE_META                    = "meta.chinese";
    public static final String BIOLOGY                                  = "biology";
    public static final String BIOLOGY_META                             = "meta.biology";
    public static final String POKER                                    = "poker";
    public static final String POKER_META                               = "meta.poker";
    public static final String MATHEMATICA                              = "mathematica";
    public static final String MATHEMATICA_META                         = "meta.mathematica";
    public static final String COGNITIVE_SCIENCES                       = "cogsci";
    public static final String COGNITIVE_SCIENCES_META                  = "meta.cogsci";
    public static final String THE_GREAT_OUTDOORS                       = "outdoors";
    public static final String THE_GREAT_OUTDOORS_META                  = "meta.outdoors";
    public static final String MARTIAL_ARTS                             = "martialarts";
    public static final String MARTIAL_ARTS_META                        = "meta.martialarts";
    public static final String SPORTS                                   = "sports";
    public static final String SPORTS_META                              = "meta.sports";
    public static final String ACADEMIA                                 = "academia";
    public static final String ACADEMIA_META                            = "meta.academia";
    public static final String COMPUTER_SCIENCE                         = "cs";
    public static final String COMPUTER_SCIENCE_META                    = "meta.cs";
    public static final String THE_WORKPLACE                            = "workplace";
    public static final String THE_WORKPLACE_META                       = "meta.workplace";
    public static final String WINDOWS_PHONE                            = "windowsphone";
    public static final String WINDOWS_PHONE_META                       = "meta.windowsphone";
    public static final String CHEMISTRY                                = "chemistry";
    public static final String CHEMISTRY_META                           = "meta.chemistry";
    public static final String CHESS                                    = "chess";
    public static final String CHESS_META                               = "meta.chess";
    public static final String RASPBERRY_PI                             = "raspberrypi";
    public static final String RASPBERRY_PI_META                        = "meta.raspberrypi";
    public static final String RUSSIAN_LANGUAGE                         = "russian";
    public static final String RUSSIAN_LANGUAGE_META                    = "meta.russian";
    public static final String ISLAM                                    = "islam";
    public static final String ISLAM_META                               = "meta.islam";
    public static final String SALESFORCE                               = "salesforce";
    public static final String SALESFORCE_META                          = "meta.salesforce";
    public static final String ASK_PATENTS                              = "patents";
    public static final String ASK_PATENTS_META                         = "meta.patents";
    public static final String GENEALOGY_FAMILY_HISTORY                 = "genealogy";
    public static final String GENEALOGY_FAMILY_HISTORY_META            = "meta.genealogy";
    public static final String ROBOTICS                                 = "robotics";
    public static final String ROBOTICS_META                            = "meta.robotics";
    public static final String EXPRESSIONENGINE_ANSWERS                 = "expressionengine";
    public static final String EXPRESSIONENGINE_ANSWERS_META            = "meta.expressionengine";
    public static final String POLITICS                                 = "politics";
    public static final String POLITICS_META                            = "meta.politics";
    public static final String ANIME_MANGA                              = "anime";
    public static final String ANIME_MANGA_META                         = "meta.anime";
    public static final String MAGENTO                                  = "magento";
    public static final String MAGENTO_META                             = "meta.magento";
    public static final String ENGLISH_LANGUAGE_LEARNERS                = "ell";
    public static final String ENGLISH_LANGUAGE_LEARNERS_META           = "meta.ell";
    public static final String SUSTAINABLE_LIVING                       = "sustainability";
    public static final String SUSTAINABLE_LIVING_META                  = "meta.sustainability";
    public static final String TRIDION                                  = "tridion";
    public static final String TRIDION_META                             = "meta.tridion";
    public static final String REVERSE_ENGINEERING                      = "reverseengineering";
    public static final String REVERSE_ENGINEERING_META                 = "meta.reverseengineering";
    public static final String NETWORK_ENGINEERING                      = "networkengineering";
    public static final String NETWORK_ENGINEERING_META                 = "meta.networkengineering";
    public static final String OPEN_DATA                                = "opendata";
    public static final String OPEN_DATA_META                           = "meta.opendata";
    public static final String FREELANCING                              = "freelancing";
    public static final String FREELANCING_META                         = "meta.freelancing";
    public static final String BLENDER                                  = "blender";
    public static final String BLENDER_META                             = "meta.blender";
    public static final String MATHOVERFLOW                             = "mathoverflow.net";
    public static final String MATHOVERFLOW_META                        = "meta.mathoverflow.net";
    public static final String SPACE_EXPLORATION                        = "space";
    public static final String SPACE_EXPLORATION_META                   = "meta.space";
    public static final String SOUND_DESIGN                             = "sound";
    public static final String SOUND_DESIGN_META                        = "meta.sound";
    public static final String ASTRONOMY                                = "astronomy";
    public static final String ASTRONOMY_META                           = "meta.astronomy";
    public static final String TOR                                      = "tor";
    public static final String TOR_META                                 = "meta.tor";
    public static final String PETS                                     = "pets";
    public static final String PETS_META                                = "meta.pets";
    public static final String AMATEUR_RADIO                            = "ham";
    public static final String AMATEUR_RADIO_META                       = "meta.ham";
    public static final String ITALIAN_LANGUAGE                         = "italian";
    public static final String ITALIAN_LANGUAGE_META                    = "meta.italian";
    public static final String STACK_OVERFLOW_EM_PORTUGUES              = "pt.stackoverflow";
    public static final String STACK_OVERFLOW_EM_PORTUGUES_META         = "meta.pt.stackoverflow";
    public static final String AVIATION                                 = "aviation";
    public static final String AVIATION_META                            = "meta.aviation";
    public static final String EBOOKS                                   = "ebooks";
    public static final String EBOOKS_META                              = "meta.ebooks";
    public static final String BEER                                     = "beer";
    public static final String BEER_META                                = "meta.beer";
    public static final String SOFTWARE_RECOMMENDATIONS                 = "softwarerecs";
    public static final String SOFTWARE_RECOMMENDATIONS_META            = "meta.softwarerecs";
    public static final String ARDUINO                                  = "arduino";
    public static final String ARDUINO_META                             = "meta.arduino";
    public static final String EXPATRIATES                              = "expatriates";
    public static final String EXPATRIATES_META                         = "meta.expatriates";
    public static final String MATHEMATICS_EDUCATORS                    = "matheducators";
    public static final String MATHEMATICS_EDUCATORS_META               = "meta.matheducators";

    
    /* 
     * Filters in parameters for the url are obtained from the stack exchanges site" 
     */
    
    /**
     * Users
     */
    //get all users
    @GET("/users")
    Call<Item> getUsers(@Query("page") int page, @Query("key") String key, @Query("site") String site);
    
    /**
     * Answers
     */
    //get all answers
    @GET("/answers?filter=!-*f(6rzfcVz5")
    Call<AnswerItem> getAnswers(@Query("page") int page, @Query("key") String key, @Query("site") String site);
    
    @GET("/answers?filter=!-*f(6rzfcVz5")
    Call<AnswerItem> getAnswersByDate(@Query("page") int page, @Query("key") String key, @Query("site") String site, @Query("fromdate") Long from, @Query("todate") Long to);
    
    //get answer by id
    @GET("/answers/{id}?filter=!9YdnSMKKT")
    Call<AnswerItem> getAnswerById(@Path("id") int answerId, @Query("key") String key, @Query("site") String site);
    
    //get answers for question
    @GET("/questions/{id}/answers?filter=!9YdnSMKKT")
    Call<AnswerItem> getAnswersbyQuestionId(@Path("id") int questionId, @Query("key") String key, @Query("site") String site);

    /**
     * Questions
     */
    
    //get all questions
    @GET("/questions?filter=!9YdnSIN18")
    Call<QuestionItem> getQuestions(@Query("page") int page, @Query("key") String key, @Query("site") String site);
    
    @GET("/questions?filter=!9YdnSIN18")
    Call<QuestionItem> getQuestionsByDate(@Query("page") int page, @Query("key") String key, @Query("site") String site, @Query("fromdate") Long from, @Query("todate") Long to);
    
    //get questions by id
    @GET("/questions/{id}?filter=!-*f(6rc.lFba")
    Call<QuestionItem> getQuestionsById(@Path("id") int questionId, @Query("key") String key, @Query("site") String site);
    
    /**
     * Comments
     */
    
    //get all comments
    @GET("/comments?filter=!9YdnSNaN(")
    Call<CommentItem> getCommentsByDate(@Query("page") int page, @Query("key") String key, @Query("site") String site, @Query("fromdate") Long from, @Query("todate") Long to);
    
    @GET("/comments?filter=!9YdnSNaN(")
    Call<CommentItem> getComments(@Query("page") int page, @Query("key") String key, @Query("site") String site);
    
    @GET("/comments/{id}?filter=!9YdnSNaN(")
    Call<CommentItem> getCommentById(@Path("id") int commentId, @Query("key") String key, @Query("site") String site);
    
    @GET("/questions/{id}/comments?filter=!9YdnSNaN(")
    Call<CommentItem> getCommentsbyQuestionId(@Path("id") int questionId, @Query("key") String key, @Query("site") String site);
    
    @GET("answers/{id}/comments?filter=!9YdnSNaN(")
    Call<CommentItem> getCommentsbyAnswerId(@Path("id") int answerId, @Query("key") String key, @Query("site") String site);
    
    //get tagged searches
    
    @GET("/search?filter=!9YdnSIN18")
    Call<QuestionItem> getSearchTags(@Query("page") int page, @Query("tagged") String tag, @Query("key") String key, @Query("site") String site);
    
    @GET("/search?filter=!9YdnSIN18")
    Call<QuestionItem> getSearchTagsByDate(@Query("page") int page, @Query("tagged") String tag, @Query("key") String key,@Query("site") String site, @Query("fromdate") Long fromdate, @Query("todate") Long todate);
    
    
    
    
    

}
