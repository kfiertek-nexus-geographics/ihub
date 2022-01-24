import "./codemirror.js";

class WebShell
{
  constructor(inputId, outputId, executeId)
  {
    this.inputElem = document.getElementById(inputId);
    this.outputElem = document.getElementById(outputId);
    this.executeButton = document.getElementById(executeId);

    const editorElem = document.createElement("div");
    editorElem.className = "cm-editor-holder";
    this.inputElem.appendChild(editorElem);

    this.executeButton.addEventListener("click", () => this.execute());

    const { basicSetup } = CM["@codemirror/basic-setup"];
    const { keymap, highlightSpecialChars, highlightActiveLine,
      drawSelection, EditorView } = CM["@codemirror/view"];
    const { lineNumbers, highlightActiveLineGutter} = CM["@codemirror/gutter"];
    const { history, historyKeymap } = CM["@codemirror/history"];
    const { defaultKeymap } = CM["@codemirror/commands"];
    const { bracketMatching } = CM["@codemirror/matchbrackets"];
    const { foldGutter, foldKeymap } = CM["@codemirror/fold"];
    const { javascript, javascriptLanguage } = CM["@codemirror/lang-javascript"];
    const { json, jsonLanguage } = CM["@codemirror/lang-json"];
    const { defaultHighlightStyle } = CM["@codemirror/highlight"];
    const { searchKeymap, highlightSelectionMatches } = CM["@codemirror/search"];
    const { indentOnInput } = CM["@codemirror/language"];
    const { EditorState } = CM["@codemirror/state"];

    let theme = EditorView.theme({
      "&.cm-focused .cm-cursor" : {
        borderLeftColor: "#000",
        borderLeftWidth: "2px"
      },
      "&.cm-focused .cm-matchingBracket" : {
        "backgroundColor" : "yellow",
        "color" : "black"
      },
      "& .ͼa" : {
        "color" : "#444",
        "fontWeight" : "bold"
      },
      "& .ͼl" : {
        "color" : "#808080"
      },
      "& .ͼf" : {
        "color" : "#8080e0"
      },
      "& .ͼd" : {
        "color" : "#2020ff"
      },
      "& .ͼb" : {
        "color" : "#008000"
      },
      "& .cm-wrap" : {
        "height" : "100%"
      },
      "& .cm-scroller" : {
        "overflow" : "auto"
      }
    });

    this.editorView = new EditorView(
    {
      parent: editorElem
    });

    const extensions = [
      lineNumbers(),
      highlightActiveLineGutter(),
      highlightSpecialChars(),
      history(),
      foldGutter(),
      drawSelection(),
      EditorState.allowMultipleSelections.of(true),
      indentOnInput(),
      defaultHighlightStyle.fallback,
      bracketMatching(),
      highlightActiveLine(),
      highlightSelectionMatches(),
      keymap.of([
        ...defaultKeymap,
        ...searchKeymap,
        ...historyKeymap,
        ...foldKeymap
      ]),
      javascript(),
      theme];

    let editorState = EditorState.create(
    {
      doc: "",
      extensions : extensions
    });

    this.editorView.setState(editorState);
  }

  execute()
  {
    let code = this.editorView.state.doc.toString();

    const xhr = new XMLHttpRequest();
    xhr.open("POST", '/webshell', true);

    xhr.setRequestHeader("Content-Type", "text/plain");

    xhr.onload = () =>
    {
      let result = xhr.responseText;
      this.outputElem.innerHTML = result;
      if (xhr.status === 200)
      {
        this.outputElem.classList.remove("error");
      }
      else
      {
        this.outputElem.classList.add("error");
      }
    };
    xhr.onerror = () =>
    {
      this.outputElem.innerHTML = "Error: " + (xhr.statusText || xhr.status);
      this.outputElem.classList.add("error");
    };
    xhr.send(code);
  }

}

export { WebShell };
