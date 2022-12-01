
function checkout(cart) {
    document.getElementById("cart").value=JSON.stringify(cart);

    document.getElementById("checkout-form").submit();
}


async function removeFromCart(id, userId) {
    const json = `{"id": "${id}", "userId": "${userId}"}`

    const response = await fetch("/services/shoppingCart", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: json
    }).catch(function (){
        console.log("error");
    })

    let updatedCartHtml = '';
    
    if (response.status === 202) {

        let formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        });
        let updatedSubtotal = 0;
        const updatedCartItems = [];

        for (let item of cart.cartItems) {
            console.log("id to be removed is: " + id);
            console.log("id of current Item is: " + item.id);
            if (item.id !== id) {
                let price = formatter.format(item.productPrice);
                updatedSubtotal += item.productPrice;
                updatedCartItems.push(item);
                updatedCartHtml +=
                    `
                    <li class="shopping-cart-item">
                        <div class="shopping-cart-item-div">
                            <div class="order-product-image">
                                <img src="${item.productImageUrl}" alt="${item.productName}">
                            </div>
                            <div class="order-product-name">
                                <span><a href="/product/${item.productName}">${item.productName}</a></span>
                            </div>
                            <div class="order-product-quantity">
                                <span>${item.quantity} Qty.</span>
                            </div>
                            <div class="order-product-unit-price">
                                <span>${price}</span>
                            </div>
                            <div class="remove-item">
                                <button class="remove-item-button" type="button" onclick="removeFromCart('${item.id}', '${item.userId}')"  form="remove-from-cart"><span class="remove-item-icon"></span></button>
                            </div>
                        </div>
                    </li>
                    `
            }
        }

        cart.cartItems = updatedCartItems;

        if (cart.cartItems.length === 0) {
            document.querySelector(".shopping-cart-item-container").innerHTML =
                `
                <li><h1>Your shopping cart is empty</h1></li>
                `;
            document.getElementById("checkout-button").style.display = "none";
            document.getElementById("shopping-cart-updatedSubtotal").innerHTML = "$0.00";
        }
        else {
        updatedSubtotal = formatter.format(updatedSubtotal);
        document.getElementById("shopping-cart-updatedSubtotal").innerHTML = `${updatedSubtotal}`
        document.querySelector(".shopping-cart-item-container").innerHTML = updatedCartHtml;
        }
    }
}
