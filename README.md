# <u>Gender Gap Analyser</u>
A program
written in Java for my bachelor's degree thesis
that downloads a dataset of men's and women's salaries from the United States,
from the U.S. Department of Labor's website,
and that analyses the salaries, creates graphs and predictions,
generates a PDF report that contains an evolution graph, the interpretations of the evolutions and a table with all the data used, and can mail the PDF report to the user's email address.<br>
It uses JavaFX in order to have a GUI that's not cluttered and that's easy and pleasant to use.

### Changelog
<ul>
<li>
<u>01.07.2023</u><br>
- Separated the dark overlay from the main menu prompt/confirmation/waiting boxes,
and now there's only one in the whole menu.
It still shows only with the said boxes, but internally there isn't an overlay wrapped with each box anymore.<br>
- Repurposed the existing AnimatedSwitcher to animate just the prompt/confirmation/waiting boxes.<br>
- Added a new AnimatedSwitcher to animate the dark overlay fading in.<br>
- Changed the boxes' animation from a fade in to a zoom in.<br>
- Applied the zoom also for the prediction deletion confirmation box.<br>
- Applied the changes and animations above for the main menu in French and Romanian too.<br>
- Made the mail button traversable by the tab key upon closing the prediction deletion confirmation box.<br>
- Removed unnecessary instructions in code
(e.g.: making the main menu buttons not traversable by the tab key after submitting a valid prediction value,
before showing the waiting box
and beginning the prediction. The main menu buttons were set
to be not traversable before showing the prediction input prompt).<br>
- Updated the README to include this changelog, which will be updated with every commit.
</li>
<br>
<li>
<u>30.06.2023</u><br>
- Fixed theme switching transition
so that the transition plays when switching themes after changing the display language too.<br>
- Added a transition when navigating between the graph and the data interpretation pages.<br>
- Added a transition when invoking the prediction and the email prompts on the main menu.<br>
</li>
<br>
<li>
<u>26.06.2023</u><br>
- Introduced a transition to smoothen the switch between dark mode and light mode<br>
- Indented the code in DataProcessing.createPDF()<br>
- Added a little, empty, nonfunctional hyperlink in all the FXML files, at the top of the hierarchy,
so that when a page displays, none of the visible elements on the page that are focus traversable are initially focused,
so that the page is displayed with nothing highlighted<br>
- Added more comments and explications in the SendEmailInBackground class
</li>
<br>
<li>
<u>24.06.2023</u><br> 
- Changed the way that DataProcessing.createPDF() functions,
so that the PDF report does not get opened from within this function,
but from Main.generatePDF(),
so that the report doesn't open anymore when the user requests to have it sent by email.<br>
- Changed the alerts throughout the app to also have dark mode when the application is set to dark mode.<br>
- Changed the alerts to not have the operating system's window decorations
(title bar, borders) so that the title bar won't be white on an alert which is in dark mode.
</li>
</ul>