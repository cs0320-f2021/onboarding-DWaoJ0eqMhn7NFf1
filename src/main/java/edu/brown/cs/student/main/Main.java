package edu.brown.cs.student.main;

import java.io.*;
import java.util.*;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * The Main class of our project. This is where execution begins.
 */
public final class Main {

  // use port 4567 by default when running server
  private static final int DEFAULT_PORT = 4567;

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    // set up parsing of command line flags
    OptionParser parser = new OptionParser();

    // "./run --gui" will start a web server
    parser.accepts("gui");

    // use "--port <n>" to specify what port on which the server runs
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);

    OptionSet options = parser.parse(args);
    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }

    List<List<String>> stars = new ArrayList<>();

    // TODO: Add your REPL here!
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String input;
      while ((input = br.readLine()) != null) {
        try {
          input = input.trim();
          String[] arguments = input.split(" ");
//          System.out.println(arguments[0]);
          // TODO: complete your REPL by adding commands for addition "add" and subtraction
          //  "subtract"

          switch (arguments[0]) {
            case "add":
              MathBot addbot = new MathBot();
              System.out.println(addbot.add(Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2])));
              break;

            case "subtract":
              MathBot subbot = new MathBot();
              System.out.println(subbot.subtract(Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2])));
              break;

            case "stars":
              if (arguments.length != 2) {
                System.out.println("ERROR: Invalid input for REPL");
                continue;
              }
              stars = new ArrayList<>();
              BufferedReader reader = new BufferedReader(new FileReader(arguments[1]));
              reader.readLine(); // to skip the property names
              String line;
              while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length != 5) {
                  System.out.println("ERROR: Invalid input for REPL");
                  continue;
                }
                stars.add(Arrays.asList(values));
              }
              System.out.println(stars);
              break;
            case "naive_neighbors":

              if (arguments.length < 3) {
                System.out.println("ERROR: Invalid input for REPL");
                continue;
              }
              int numNeighbors = Integer.parseInt(arguments[1]);
              if (numNeighbors == 0) {
                continue;
              }
              boolean digit = false;
              Double[] origin = null;
              String name = "";
              if (Character.isDigit(arguments[2].charAt(0))) { // if <k> <x> <y> <z>
                digit = true;
                origin = new Double[]{Double.parseDouble(arguments[2]), Double.parseDouble(arguments[3]), Double.parseDouble(arguments[4])};

              } else {
                digit = false;

                for (int i = 2; i < arguments.length; i++) {
                  name = name + arguments[i];
                  if (i < arguments.length - 1) {
                    name = name + " ";
                  }
                }
                name = name.replaceAll("\"", ""); // replace quotes

                for (List<String> star : stars) {
                  if (star.get(1).equals(name)) {
                    origin = new Double[]{Double.parseDouble(star.get(2)), Double.parseDouble(star.get(3)), Double.parseDouble(star.get(4))};
                    break;
                  }
                }
              }
              PriorityQueue<Map.Entry<Double, String>> pq = new PriorityQueue<>(numNeighbors, Map.Entry.comparingByKey(Comparator.reverseOrder()));
              for (List<String> star : stars) {
                if (!digit && star.get(1).equals(name)) { // don't factor in same star
                  continue;
                }
                Double distance = Math.sqrt(Math.pow(Double.parseDouble(star.get(2)) - origin[0], 2) + Math.pow(Double.parseDouble(star.get(3)) - origin[1], 2) + Math.pow(Double.parseDouble(star.get(4)) - origin[2], 2));

                if (pq.size() == numNeighbors) {
                  Map.Entry<Double, String> last = pq.poll();
                  if (last.getKey() > distance) { // if new star is less
                    Map.Entry<Double, String> entry = Map.entry(distance, star.get(0));
                    pq.add(entry);
                  } else if (last.getKey() == distance) { // if equal
                    int rand = (int) (Math.random() * 2 + 1);
                    if (rand == 1) {
                      pq.add(last);
                    } else {
                      Map.Entry<Double, String> entry = Map.entry(distance, star.get(0));
                      pq.add(entry);
                    }
                  } else { // if new star is more
                    pq.add(last);
                  }
                } else {
                  Map.Entry<Double, String> entry = Map.entry(distance, star.get(0));
                  pq.add(entry);
                }
              }
              Iterator<Map.Entry<Double, String>> itr = pq.iterator();
              while (itr.hasNext()) {
                Map.Entry<Double, String> entry = itr.next();
                System.out.println(entry.getValue());
              }
              break;
            default:
              System.out.println("ERROR: Invalid input for REPL");
              break;
          }

        } catch (Exception e) {
          // e.printStackTrace();
          System.out.println("ERROR: We couldn't process your input");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("ERROR: Invalid input for REPL");
    }

  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration(Configuration.VERSION_2_3_0);

    // this is the directory where FreeMarker templates are placed
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    // set port to run the server on
    Spark.port(port);

    // specify location of static resources (HTML, CSS, JS, images, etc.)
    Spark.externalStaticFileLocation("src/main/resources/static");

    // when there's a server error, use ExceptionPrinter to display error on GUI
    Spark.exception(Exception.class, new ExceptionPrinter());

    // initialize FreeMarker template engine (converts .ftl templates to HTML)
    FreeMarkerEngine freeMarker = createEngine();

    // setup Spark Routes
    Spark.get("/", new MainHandler(), freeMarker);
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request req, Response res) {
      // status 500 generally means there was an internal server error
      res.status(500);

      // write stack trace to GUI
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  /**
   * A handler to serve the site's main page.
   *
   * @return ModelAndView to render.
   * (main.ftl).
   */
  private static class MainHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      // this is a map of variables that are used in the FreeMarker template
      Map<String, Object> variables = ImmutableMap.of("title",
          "Go go GUI");

      return new ModelAndView(variables, "main.ftl");
    }
  }
}
