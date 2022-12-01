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
                <a href="/product/${product.name}">
                    <li>
                        <img src="${product.imageUrl}" alt="${product.name}">
                        <h3>${price}</h3>
                        <p>${product.name}</p>
                    </li>
                </a>
            `);
       }
    }

    document.querySelector(".product-catalog").innerHTML = results.join('\n');
}


const main = async () => {
    refreshSearch();
    document.getElementById("search-bar").addEventListener("keyup", refreshSearch);
}

window.addEventListener('DOMContentLoaded', main);
