/*
 * Copyright (c) Avaloq Evolution AG
 * Allmendstr. 140, 8027 Zürich, Switzerland, http://www.avaloq.com
 * All Rights Reserved.
 *
 * Author: Hafner Ullrich
 */

using System;
using System.ComponentModel;
using System.Diagnostics.CodeAnalysis;
using System.Windows.Forms;
using Avaloq.Utilities;
using log4net;

namespace Avaloq.SmartClient.Utilities
{
    /// <summary>
    /// Acts as mediator between an <see cref="Action"/> and a clickable Windows Forms control.
    /// </summary>
    /// <remarks>
    /// An <see cref="ActionBinding"/> brings the Java Action concept to any clickable Windows Forms control. An <see cref="ActionBinding"/>
    /// connects the following properties and events:
    /// <list type="bullet">
    /// <item><see cref="System.Windows.Forms.Control.Enabled"/> of a control to <see cref="Action.Runnable"/> of an action</item>
    /// <item><see cref="Control.Name"/> of a control to <see cref="Action.Name"/> of an action</item>
    /// <item><see cref="System.Windows.Forms.Control.Text"/> of a control to <see cref="Action.Text"/> of an action</item>
    /// <item><see cref="System.Windows.Forms.Control.Click"/> of a control to <see cref="Action.Run"/> of an action</item>
    /// </list>
    /// Whenever an action property changes, the corresponding control property is adjusted accordingly. If the control is clicked then
    /// the <see cref="Action.Run"/> method is invoked. Don't forget to call <see cref="Dispose()"/> on this object in order to cleanup the event
    /// listeners.
    /// </remarks>
    /// <example>
    /// <code>
    /// class HelloWorldAction : Action {
    ///     public HelloWorldAction()
    ///         : base("Hello") {
    ///     }
    ///
    ///     public override void Run() {
    ///         Debug.WriteLine("Hello World");
    ///     }
    /// }
    ///
    /// [...]
    ///
    /// Action action = new HelloWorldAction();
    /// Button button = new Button();
    /// ActionBinding binding = ActionBinding.Bind(button, action);
    ///
    /// [...]
    /// FIXME: here is a fixme
    /// binding.Dispose();
    /// </code>
    /// </example>
    public class ActionBinding : IDisposable {
        /// <summary>
        /// The logger to trace debug messages.
        /// </summary>
        static readonly ILog logger = LogManager.GetLogger(typeof(ActionBinding));

        /// <summary>
        /// The action to connect to the control.
        /// </summary>
        readonly Action action;

        /// <summary>
        /// The control to connect to the action.
        /// </summary>
        readonly Control control;

        /// <summary>
        /// Binds the specified action to a control.
        /// </summary>
        /// <param name="control">the control to connect to the action</param>
        /// <param name="action">the action to connect to the control</param>
        [SuppressMessage("Microsoft.Usage", "CA1806")]
        [SuppressMessage("Avaloq.Design", "ParameterContractRule")]
        public static ActionBinding Bind(Action action, Control control) {
            return new ActionBinding(action, control);
        }

        /// <summary>
        /// Creates a new control adapter.
        /// </summary>
        /// <param name="control">the control to connect to the action</param>
        /// <param name="action">the action to connect to the control</param>
        public ActionBinding(Action action, Control control) {
            #region Preconditions

            Contract.RequireNotNullArgument(control, "control");
            Contract.RequireNotNullArgument(action, "action");

            #endregion

            this.action = action;
            this.action.PropertyChanged += new PropertyChangedEventHandler(UpdateControlProperties);

            this.control = control;
            this.control.Click += new EventHandler(RunAction);

            UpdateControlProperties();
        }

        /// <summary>
        /// Handles a property changed event raised by the action. All
        /// control properties are updated.
        /// </summary>
        /// <param name="sender">not used</param>
        /// <param name="e">not used</param>
        void UpdateControlProperties(object sender, PropertyChangedEventArgs e) {
            #region Preconditions

            Contract.RequireNotNullArgument(e, "e");

            #endregion

            if (logger.IsDebugEnabled) {
                logger.Debug(string.Format("Property '{0}' of action '{1}' changed from '{2}' to '{3}'.",
                                           e.PropertyName, action.Name, ChangedPropertyEventArgs.GetOldValue(e), ChangedPropertyEventArgs.GetNewValue(e)));
            }
            UpdateControlProperties();
        }

        /// <summary>
        /// Updates all control properties with the values provided by the corresponding action properties.
        /// </summary>
        void UpdateControlProperties() {
            if (!alreadyDisposed) {
                control.Enabled = action.Runnable;
                control.Text = action.Text;
                control.Name = action.Name;
            }
        }

        /// <summary>
        /// Handles a <see cref="System.Windows.Forms.Control.Click"/> event raised by the control.
        /// Executes the action method <see cref="Action.Run()"/>.
        /// </summary>
        /// <param name="sender">not used</param>
        /// <param name="e">not used</param>
        void RunAction(object sender, EventArgs e) {
            if (logger.IsDebugEnabled) {
                logger.Debug(string.Format("Control '{0}' has been clicked. Running corresponding action.", action.Name));
            }
            action.Run();
        }

        #region Dispose Pattern

        /// <summary>
        /// Indicates wether this instance already has been disposed.
        /// </summary>
        bool alreadyDisposed;

        /// <summary>
        /// Releases unmanaged resources and performs other cleanup operations before this
        /// <see cref="ActionBinding"/> instance is reclaimed by garbage collection.
        /// </summary>
        ~ActionBinding() {
            Dispose(false);
        }

        /// <summary>
        /// Performs application-defined tasks associated with freeing, releasing, or resetting unmanaged resources of
        /// this <see cref="ActionBinding"/> instance.
        /// </summary>
        /// <param name="isDisposing">if set to <c>true</c> then we are called by <see cref="Dispose()"/>,
        /// otherwise by the destructor</param>
        /// <remarks>
        /// If your derived class needs to perform additional cleanup, override this method and call it after the derived class cleanup.
        /// </remarks>
        protected virtual void Dispose(bool isDisposing) {
            if (alreadyDisposed) {
                return;
            }
            if (isDisposing) {
                action.PropertyChanged -= new PropertyChangedEventHandler(UpdateControlProperties);
                control.Click -= new EventHandler(RunAction);
            }

            alreadyDisposed = true;
        }

        ///<summary>
        /// Performs application-defined tasks associated with freeing, releasing, or resetting unmanaged resources.
        ///</summary>
        public void Dispose() {
            Dispose(true);
            GC.SuppressFinalize(true);
        }

        #endregion
    }
}