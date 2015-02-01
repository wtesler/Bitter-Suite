<b>Bitter-Suite</b>
Coinbase interface by Will Tesler</b>

-See CoinbaseClient.java for the main functionality of the program.

-See GuiMain.java and LayoutController.java for application GUI and lifecycle.

-See some examples in Tests.java.

-See mongo package for code related to our mongo database.

Specification:

<b>CoinbaseClient<b/> contains methods for interfacing with the Coinbase API Servers. It has two uses:

1. It opens socketed connections with the market (https://docs.exchange.coinbase.com/#websocket-feed).

2. It issues GET requests such as getHistoricalData() with it's static methods.

Application Frame:

-GuiMain.java sets the stage and displays the opening scene (http://docs.oracle.com/javase/8/javafx/api/javafx/scene/Scene.html)

-Layout.fxml (layout description language) describes the layout of the window. These layouts were generated in the Java Stage Builder application.

-LayoutController.java is responsible for drawing and updating the line chart. It registers a listener with CoinbaseClient to in order to receive updated market information.

Agents:

-BaseAgent.java contains the abstract methods that any new agent must implement when wanting to interface with the updating market data. 

-If you want an agent to receive updated market data, make sure to add the agent's listener to the CoinbaseClient's list of managed listeners.

-You must implement the agent's getListener method,  which involves creating and returning a WebSocketAdapter that should (probably) override the superclass methods such as OnWebSocketConnect, OnWebSocketText, OnWebSocketError.

-Agents (can optionally) manage lists of market data such as matches, opens, completed, and received lists. That may be helpful for storing data in realtime.

<b>Implement your agent's trading strategy in makeDecision().<b/>

-This method should be called from within WebSocketAdapter.OnWebSocketText() which is implemented in getListener().


