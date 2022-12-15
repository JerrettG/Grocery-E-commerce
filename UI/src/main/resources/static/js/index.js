function fuzzyMatch(text, searchTerm) {

    searchTerm = searchTerm.replace(/\ /g, '').toLowerCase();
    return text.toLowerCase().includes(searchTerm);
}

function refreshSearch() {
    let searchTerm = document.getElementById("search-bar").value;
    let results = [];
    const formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    })
    for (let product of products) {

       let result = fuzzyMatch(product.name, searchTerm);

       if (result) {
           let price = formatter.format(product.price);
            results.push(
                `
                <div class="product-catalog-product-card" id="${product.productId}">
                    <li>
                        <a href="/product/${product.name}">
                            <img src="${product.imageUrl}" alt="${product.name}" class="product-catalog-product-image">
                            <div class="product-name-container">
                                <h3 class="product-name">${product.name}</h3>
                            </div>
                            <div class="product-price-unit-description">
                                <span style="font-family: Arial"><strong>${price}</strong></span><span> / </span><span style="font-family: Arial"><strong>${product.unitMeasurement}</strong></span>
                            </div>
                        </a>
                        <button class="product-catalog-add-to-cart-button" type="button" onclick="addToCart('${product.productId}', '${product.name}', '${product.price}', '${product.imageUrl}')">Add to cart</button>
                    </li>
                </div>
            `);
       }
    }

    document.querySelector(".product-catalog").innerHTML = results.join('\n');
}

async function addToCart(productId, name, price, productImageUrl) {
    if (!isAuthenticated) {
        window.location.href = "http://localhost:8084/oauth2/authorization/auth0";
    }
    else {
        let quantity = 1;

        let cartItem = {
            productId: productId,
            productImageUrl: productImageUrl,
            productName: name,
            productPrice: price,
            quantity: quantity
        }

        const response = await fetch(`/services/product/${name}/addToCart`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(cartItem)
        }).catch(function () {
            console.log("error");
        });
    }

}


const main = async () => {
    refreshSearch();
    document.getElementById("search-bar").addEventListener("keyup", refreshSearch);
}

window.addEventListener('DOMContentLoaded', main);
