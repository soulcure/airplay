package com.coocaa;

public class Example {

    public final static String EX_STYLE = "<?xml version=\"1.0\" standalone=\"no\"?>\n" +
            "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
            "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
            "\n" +
            "<svg width=\"100%\" height=\"100%\" version=\"1.1\"\n" +
            "xmlns=\"http://www.w3.org/2000/svg\">\n" +
            "\n" +
            "<path d=\"M153 334\n" +
            "C153 334 151 334 151 334\n" +
            "C151 339 153 344 156 344\n" +
            "C164 344 171 339 171 334\n" +
            "C171 322 164 314 156 314\n" +
            "C142 314 131 322 131 334\n" +
            "C131 350 142 364 156 364\n" +
            "C175 364 191 350 191 334\n" +
            "C191 311 175 294 156 294\n" +
            "C131 294 111 311 111 334\n" +
            "C111 361 131 384 156 384\n" +
            "C186 384 211 361 211 334\n" +
            "C211 300 186 274 156 274\"\n" +
            "style=\"fill:white;stroke:red;stroke-width:2\"/>\n" +
            "\n" +
            "</svg>";

    public final static String EX_INN = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n" +
            "  <path id=\"lineAB\" d=\"M 100 350 l 150 -300\" stroke=\"red\"\n" +
            "  stroke-width=\"3\" fill=\"none\" />\n" +
            "  <path id=\"lineBC\" d=\"M 250 50 l 150 300\" stroke=\"red\"\n" +
            "  stroke-width=\"3\" fill=\"none\" />\n" +
            "  <path d=\"M 175 200 l 150 0\" stroke=\"green\" stroke-width=\"3\"\n" +
            "  fill=\"none\" />\n" +
            "  <path d=\"M 100 350 q 150 -300 300 0\" stroke=\"purple\"\n" +
            "  stroke-width=\"1\" fill=\"none\" />\n" +
            "  <!-- Mark relevant points -->\n" +
            "  <g stroke=\"black\" stroke-width=\"3\" fill=\"black\">\n" +
            "    <circle id=\"pointA\" cx=\"100\" cy=\"350\" r=\"3\" />\n" +
            "    <circle id=\"pointB\" cx=\"250\" cy=\"50\" r=\"3\" />\n" +
            "    <circle id=\"pointC\" cx=\"400\" cy=\"350\" r=\"3\" />\n" +
            "  </g>\n" +
            "  <!-- Label the points -->\n" +
            "  <g font-size=\"30\" font=\"sans-serif\" fill=\"black\" stroke=\"none\"\n" +
            "  text-anchor=\"middle\">\n" +
            "    <text x=\"100\" y=\"350\" dx=\"-30\">AAAAAA</text>\n" +
            "    <text x=\"250\" y=\"50\" dy=\"-10\">BBBBBBB</text>\n" +
            "    <text x=\"400\" y=\"350\" dx=\"30\">CCCCCCCC</text>\n" +
            "  </g>\n" +
            "</svg>";

    public final static String Test = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n" +
            "  <path id=\"lineAB\" d=\"M 100 350 l 150 -300\" stroke=\"red\"\n" +
            "  stroke-width=\"3\" fill=\"none\" />\n" +
            "  <path id=\"lineBC\" d=\"M 250 50 l 150 300\" stroke=\"red\"\n" +
            "  stroke-width=\"3\" fill=\"none\" />\n" +
            "  <path d=\"M 175 200 l 150 0\" stroke=\"green\" stroke-width=\"3\"\n" +
            "  fill=\"none\" />\n" +
            "  <path d=\"M 100 350 q 150 -300 300 0\" stroke=\"purple\"\n" +
            "  stroke-width=\"1\" fill=\"none\" />\n" +
            "</svg>";
}
