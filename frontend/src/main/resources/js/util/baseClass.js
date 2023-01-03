import Toastify from "toastify-js";

export default class BaseClass {
    /**
     * Binds all of the methods to "this" object. These methods will now have the state of the instance object.
     * @param methods The name of each method to bind.
     * @param classInstance The instance of the class to bind the methods to.
     */
    bindClassMethods(methods, classInstance) {
        methods.forEach(method => {
            classInstance[method] = classInstance[method].bind(classInstance);
        });
    }

    formatCurrency(amount) {
        const formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        });
        return formatter.format(amount);
    }

    showMessage(message) {
        Toastify({
            text: message,
            duration: 4500,
            gravity: "top",
            position: 'right',
            close: true,
            style: {
                background: "var(--secondary-color)"
            }
        }).showToast();
    }

    toggleDropdown(event) {
        const dropButton = event.srcElement;
        const ariaExpanded = dropButton.getAttribute('aria-expanded');
        if (ariaExpanded === 'true') {
            const dropButtonContainer = dropButton.closest('.dropbutton-container');
            dropButton.setAttribute('aria-expanded', false);
            const dropdownContent = dropButtonContainer.nextElementSibling;
            dropdownContent.style.maxHeight = '40em';
        }
        else if (ariaExpanded === 'false') {
            const dropButtonContainer = dropButton.closest('.dropbutton-container');
            dropButton.setAttribute('aria-expanded', true);
            const dropdownContent = dropButtonContainer.nextElementSibling;
            dropdownContent.style.maxHeight = '0';
        }
    }

    showLoading(event, widthPercent) {
            event.innerHTML =
                `
            <div class="loading-spinner" style="width: ${widthPercent}%;"> </div>
            `
    }

    errorHandler(error) {
        Toastify({
            text: error,
            duration: 4500,
            gravity: "top",
            position: 'right',
            close: true,
            style: {
                background: "linear-gradient(to right, rgb(255, 95, 109), rgb(255, 195, 113))"
            }
        }).showToast();
    }
}
